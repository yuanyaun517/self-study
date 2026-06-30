package cn.only.hw.secondmarketserver;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;

@SpringBootTest
class SecondMarketServerApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    public void testArr() {
        String a = "https://img11.360buyimg.com/n1/jfs/t1/79043/27/17420/123219/6139689aE667b8269/37971b91f2079637.jpg,https://img11.360buyimg.com/n1/jfs/t1/79043/27/17420/123219/6139689aE667b8269/37971b91f2079637.jpg,https://img11.360buyimg.com/n1/jfs/t1/79043/27/17420/123219/6139689aE667b8269/37971b91f2079637.jpg";
        String[] imgStrs = a.split(",");
        System.out.println(Arrays.asList(imgStrs));
    }
}
