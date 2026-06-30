package cn.only.hw.secondmarketserver.dto;

import cn.only.hw.secondmarketserver.entity.Forum;
import cn.only.hw.secondmarketserver.entity.Goods;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@ApiModel("ManagerDashboardDto")
public class ManagerDashboardDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("Total users")
    private Long userCount;

    @ApiModelProperty("Total goods")
    private Long goodsCount;

    @ApiModelProperty("Pending goods")
    private Long goodsPendingCount;

    @ApiModelProperty("Approved goods")
    private Long goodsApprovedCount;

    @ApiModelProperty("Rejected goods")
    private Long goodsRejectedCount;

    @ApiModelProperty("Sold out goods")
    private Long goodsSoldOutCount;

    @ApiModelProperty("Total forums")
    private Long forumCount;

    @ApiModelProperty("Pending forums")
    private Long forumPendingCount;

    @ApiModelProperty("Approved forums")
    private Long forumApprovedCount;

    @ApiModelProperty("Rejected forums")
    private Long forumRejectedCount;

    @ApiModelProperty("Total orders")
    private Long orderCount;

    @ApiModelProperty("Unpaid orders")
    private Long unpaidOrderCount;

    @ApiModelProperty("Paid orders")
    private Long paidOrderCount;

    @ApiModelProperty("Shipping orders")
    private Long shippingOrderCount;

    @ApiModelProperty("Completed orders")
    private Long completedOrderCount;

    @ApiModelProperty("Cancelled orders")
    private Long cancelledOrderCount;

    @ApiModelProperty("Accumulated paid amount")
    private Double paidAmount;

    @ApiModelProperty("Banner count")
    private Long bannerCount;

    @ApiModelProperty("Notice count")
    private Long noticeCount;

    @ApiModelProperty("Category count")
    private Long categoryCount;

    @ApiModelProperty("Recent goods")
    private List<Goods> recentGoods = new ArrayList<Goods>();

    @ApiModelProperty("Recent forums")
    private List<Forum> recentForums = new ArrayList<Forum>();

    @ApiModelProperty("Recent orders")
    private List<OrdersDto> recentOrders = new ArrayList<OrdersDto>();

    @ApiModelProperty("Recent 7 day labels")
    private List<String> trendDates = new ArrayList<String>();

    @ApiModelProperty("Recent 7 day goods trend")
    private List<Long> goodsTrend = new ArrayList<Long>();

    @ApiModelProperty("Recent 7 day forum trend")
    private List<Long> forumTrend = new ArrayList<Long>();

    @ApiModelProperty("Recent 7 day order trend")
    private List<Long> orderTrend = new ArrayList<Long>();

    @ApiModelProperty("Recent 7 day paid amount trend")
    private List<Double> paidAmountTrend = new ArrayList<Double>();

    public Long getUserCount() {
        return userCount;
    }

    public void setUserCount(Long userCount) {
        this.userCount = userCount;
    }

    public Long getGoodsCount() {
        return goodsCount;
    }

    public void setGoodsCount(Long goodsCount) {
        this.goodsCount = goodsCount;
    }

    public Long getGoodsPendingCount() {
        return goodsPendingCount;
    }

    public void setGoodsPendingCount(Long goodsPendingCount) {
        this.goodsPendingCount = goodsPendingCount;
    }

    public Long getGoodsApprovedCount() {
        return goodsApprovedCount;
    }

    public void setGoodsApprovedCount(Long goodsApprovedCount) {
        this.goodsApprovedCount = goodsApprovedCount;
    }

    public Long getGoodsRejectedCount() {
        return goodsRejectedCount;
    }

    public void setGoodsRejectedCount(Long goodsRejectedCount) {
        this.goodsRejectedCount = goodsRejectedCount;
    }

    public Long getGoodsSoldOutCount() {
        return goodsSoldOutCount;
    }

    public void setGoodsSoldOutCount(Long goodsSoldOutCount) {
        this.goodsSoldOutCount = goodsSoldOutCount;
    }

    public Long getForumCount() {
        return forumCount;
    }

    public void setForumCount(Long forumCount) {
        this.forumCount = forumCount;
    }

    public Long getForumPendingCount() {
        return forumPendingCount;
    }

    public void setForumPendingCount(Long forumPendingCount) {
        this.forumPendingCount = forumPendingCount;
    }

    public Long getForumApprovedCount() {
        return forumApprovedCount;
    }

    public void setForumApprovedCount(Long forumApprovedCount) {
        this.forumApprovedCount = forumApprovedCount;
    }

    public Long getForumRejectedCount() {
        return forumRejectedCount;
    }

    public void setForumRejectedCount(Long forumRejectedCount) {
        this.forumRejectedCount = forumRejectedCount;
    }

    public Long getOrderCount() {
        return orderCount;
    }

    public void setOrderCount(Long orderCount) {
        this.orderCount = orderCount;
    }

    public Long getUnpaidOrderCount() {
        return unpaidOrderCount;
    }

    public void setUnpaidOrderCount(Long unpaidOrderCount) {
        this.unpaidOrderCount = unpaidOrderCount;
    }

    public Long getPaidOrderCount() {
        return paidOrderCount;
    }

    public void setPaidOrderCount(Long paidOrderCount) {
        this.paidOrderCount = paidOrderCount;
    }

    public Long getShippingOrderCount() {
        return shippingOrderCount;
    }

    public void setShippingOrderCount(Long shippingOrderCount) {
        this.shippingOrderCount = shippingOrderCount;
    }

    public Long getCompletedOrderCount() {
        return completedOrderCount;
    }

    public void setCompletedOrderCount(Long completedOrderCount) {
        this.completedOrderCount = completedOrderCount;
    }

    public Long getCancelledOrderCount() {
        return cancelledOrderCount;
    }

    public void setCancelledOrderCount(Long cancelledOrderCount) {
        this.cancelledOrderCount = cancelledOrderCount;
    }

    public Double getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(Double paidAmount) {
        this.paidAmount = paidAmount;
    }

    public Long getBannerCount() {
        return bannerCount;
    }

    public void setBannerCount(Long bannerCount) {
        this.bannerCount = bannerCount;
    }

    public Long getNoticeCount() {
        return noticeCount;
    }

    public void setNoticeCount(Long noticeCount) {
        this.noticeCount = noticeCount;
    }

    public Long getCategoryCount() {
        return categoryCount;
    }

    public void setCategoryCount(Long categoryCount) {
        this.categoryCount = categoryCount;
    }

    public List<Goods> getRecentGoods() {
        return recentGoods;
    }

    public void setRecentGoods(List<Goods> recentGoods) {
        this.recentGoods = recentGoods;
    }

    public List<Forum> getRecentForums() {
        return recentForums;
    }

    public void setRecentForums(List<Forum> recentForums) {
        this.recentForums = recentForums;
    }

    public List<OrdersDto> getRecentOrders() {
        return recentOrders;
    }

    public void setRecentOrders(List<OrdersDto> recentOrders) {
        this.recentOrders = recentOrders;
    }

    public List<String> getTrendDates() {
        return trendDates;
    }

    public void setTrendDates(List<String> trendDates) {
        this.trendDates = trendDates;
    }

    public List<Long> getGoodsTrend() {
        return goodsTrend;
    }

    public void setGoodsTrend(List<Long> goodsTrend) {
        this.goodsTrend = goodsTrend;
    }

    public List<Long> getForumTrend() {
        return forumTrend;
    }

    public void setForumTrend(List<Long> forumTrend) {
        this.forumTrend = forumTrend;
    }

    public List<Long> getOrderTrend() {
        return orderTrend;
    }

    public void setOrderTrend(List<Long> orderTrend) {
        this.orderTrend = orderTrend;
    }

    public List<Double> getPaidAmountTrend() {
        return paidAmountTrend;
    }

    public void setPaidAmountTrend(List<Double> paidAmountTrend) {
        this.paidAmountTrend = paidAmountTrend;
    }
}
