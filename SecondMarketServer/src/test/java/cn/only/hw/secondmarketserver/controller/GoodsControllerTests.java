package cn.only.hw.secondmarketserver.controller;

import cn.only.hw.secondmarketserver.entity.Catechild;
import cn.only.hw.secondmarketserver.entity.Category;
import cn.only.hw.secondmarketserver.entity.Goods;
import cn.only.hw.secondmarketserver.service.CatechildService;
import cn.only.hw.secondmarketserver.service.CategoryService;
import cn.only.hw.secondmarketserver.service.GoodsService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GoodsController.class)
class GoodsControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GoodsService goodsService;

    @MockBean
    private CatechildService catechildService;

    @MockBean
    private CategoryService categoryService;

    @Test
    void shouldResubmitRejectedGoodsWithOriginalId() throws Exception {
        Goods currentGoods = new Goods();
        currentGoods.setId(6);
        currentGoods.setSendUser(4);
        currentGoods.setManage("2");

        Category category = new Category();
        category.setCateid(8);

        when(categoryService.getById(8)).thenReturn(category);
        when(goodsService.getById(6)).thenReturn(currentGoods);
        when(goodsService.updateById(any(Goods.class))).thenReturn(true);
        when(catechildService.save(any(Catechild.class))).thenReturn(true);

        mockMvc.perform(post("/goods/resubmit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"
                                + "\"id\":6,"
                                + "\"sendUser\":4,"
                                + "\"cateid\":8,"
                                + "\"name\":\" 橘子 \","
                                + "\"type\":\" 二手闲置 \","
                                + "\"price\":5,"
                                + "\"number\":2,"
                                + "\"status\":\" 全新 \","
                                + "\"dealtypy\":\" 线下面对面交易 \","
                                + "\"imgs\":\"https://img/1.png,https://img/2.png\","
                                + "\"describes\":\" 新鲜橘子 \","
                                + "\"icon\":\"https://img/1.png\""
                                + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data").value("Resubmit success"));

        ArgumentCaptor<Goods> goodsCaptor = ArgumentCaptor.forClass(Goods.class);
        verify(goodsService).updateById(goodsCaptor.capture());
        Goods updatedGoods = goodsCaptor.getValue();
        assertEquals(6, updatedGoods.getId());
        assertEquals("橘子", updatedGoods.getName());
        assertEquals("二手闲置", updatedGoods.getType());
        assertEquals(5.0D, updatedGoods.getPrice());
        assertEquals(2, updatedGoods.getNumber());
        assertEquals("全新", updatedGoods.getStatus());
        assertEquals("线下面对面交易", updatedGoods.getDealtypy());
        assertEquals("https://img/1.png,https://img/2.png", updatedGoods.getImgs());
        assertEquals("新鲜橘子", updatedGoods.getDescribes());
        assertEquals("https://img/1.png", updatedGoods.getIcon());
        assertEquals("0", updatedGoods.getManage());
        assertEquals(8, updatedGoods.getCateid());
    }

    @Test
    void shouldRejectResubmitWhenGoodsIsNotRejected() throws Exception {
        Goods currentGoods = new Goods();
        currentGoods.setId(6);
        currentGoods.setSendUser(4);
        currentGoods.setManage("1");

        Category category = new Category();
        category.setCateid(8);

        when(categoryService.getById(8)).thenReturn(category);
        when(goodsService.getById(6)).thenReturn(currentGoods);

        mockMvc.perform(post("/goods/resubmit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"
                                + "\"id\":6,"
                                + "\"sendUser\":4,"
                                + "\"cateid\":8,"
                                + "\"name\":\"橘子\","
                                + "\"type\":\"二手闲置\","
                                + "\"price\":5,"
                                + "\"number\":1,"
                                + "\"status\":\"全新\","
                                + "\"dealtypy\":\"线下面对面交易\""
                                + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("Only rejected goods can be resubmitted"));

        verify(goodsService, never()).updateById(any(Goods.class));
    }

    @Test
    void shouldAttachCateidWhenOwnerLoadsGoodsDetail() throws Exception {
        Goods goods = new Goods();
        goods.setId(6);
        goods.setSendUser(4);
        goods.setManage("2");
        goods.setNumber(1);
        goods.setName("橘子");

        Catechild catechild = new Catechild();
        catechild.setCateid(8);

        when(goodsService.getById(6)).thenReturn(goods);
        when(catechildService.list(any())).thenReturn(Collections.singletonList(catechild));

        mockMvc.perform(post("/goods/getById")
                        .param("id", "6")
                        .param("userid", "4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.id").value(6))
                .andExpect(jsonPath("$.data.cateid").value(8));
    }
}
