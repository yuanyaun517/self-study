package cn.only.hw.secondmarketserver.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class OrderReviewDto implements Serializable {

    private static final long serialVersionUID = -2724287013385023204L;

    private Integer id;

    private Integer userId;

    private Integer rating;

    private String reviewContent;
}
