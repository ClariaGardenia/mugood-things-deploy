package com.muGood.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;

@TableName("banner")
public class Banner {
    private Long id;
    private String imgUrl;
    private String hrefUrl;
    private Integer distributionSite;
    private String title;
    private Integer sortOrder;
    private Integer status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getHrefUrl() {
        return hrefUrl;
    }

    public void setHrefUrl(String hrefUrl) {
        this.hrefUrl = hrefUrl;
    }

    public Integer getDistributionSite() {
        return distributionSite;
    }

    public void setDistributionSite(Integer distributionSite) {
        this.distributionSite = distributionSite;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
