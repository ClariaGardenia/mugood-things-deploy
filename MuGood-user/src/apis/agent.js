import httpInstance from '@/utils/http'

const baseURL = 'http://localhost:8080'

export const chatWithCustomerAgentAPI = (message, sessionId) => {
  return httpInstance({
    url: '/agent/customer-service/chat',
    method: 'POST',
    data: {
      message,
      sessionId
    }
  })
}

export const streamCustomerAgentAPI = async (message, sessionId, handlers = {}) => {
  const response = await fetch(`${baseURL}/agent/customer-service/chat/stream`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Accept: 'text/event-stream'
    },
    body: JSON.stringify({ message, sessionId })
  })

  if (!response.ok || !response.body) {
    throw new Error(`Customer agent request failed: ${response.status}`)
  }

  const reader = response.body.getReader()
  const decoder = new TextDecoder('utf-8')
  let buffer = ''

  while (true) {
    const { value, done } = await reader.read()
    if (done) break

    buffer += decoder.decode(value, { stream: true })
    const frames = buffer.split(/\r?\n\r?\n/)
    buffer = frames.pop() || ''

    for (const frame of frames) {
      const data = frame
        .split(/\r?\n/)
        .filter(line => line.startsWith('data:'))
        .map(line => line.slice(5).trimStart())
        .join('\n')

      if (!data) continue

      const payload = JSON.parse(data)
      if (payload.type === 'goods') await handlers.onGoods?.(payload.goods || [])
      if (payload.type === 'token') await handlers.onToken?.(payload.content || '')
      if (payload.type === 'error') await handlers.onError?.(payload.content || '')
      if (payload.type === 'done') await handlers.onDone?.()
    }
  }
}
