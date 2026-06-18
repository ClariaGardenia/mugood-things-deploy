# fresh-rabbit-admin

Fresh Rabbit Vue 3 management console.

## Stack

- Vue 3
- Vite
- Vue Router
- Pinia
- Element Plus
- Axios

## Run

Start the backend first:

```powershell
cd ../fresh-rabbit-server
$env:JAVA_HOME='D:\Program Files\JDK\jdk-21'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
mvn -pl fresh-rabbit-web -am spring-boot:run
```

Then start the admin frontend:

```powershell
npm install
npm run dev
```

The admin app runs on:

```text
http://localhost:5174
```

Default login:

```text
admin / 123456
```

