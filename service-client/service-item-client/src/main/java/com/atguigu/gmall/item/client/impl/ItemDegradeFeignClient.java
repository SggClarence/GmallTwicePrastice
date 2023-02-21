package com.atguigu.gmall.item.client.impl;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.item.client.ItemFeignClient;
import org.springframework.stereotype.Component;

/**
 * projectName: GmallTwicePrastice
 * 吾常终日而思矣 不如须臾之所学!
 *
 * @author: Clarence
 * time: 2023/2/18 2:12 周六
 * description:
 */
@Component
public class ItemDegradeFeignClient implements ItemFeignClient {
    /**
     * @param skuId
     * @return
     */
    @Override
    public Result getItem(Long skuId) {
        return null;
    }
}
