package com.thelastcodebenders.follower.client.rate;

import com.thelastcodebenders.follower.client.rate.dto.CurrencyRatesItem;
import com.thelastcodebenders.follower.exception.DetectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.text.DecimalFormat;

@Service
public class CurrencyRateService {
    private static final Logger log = LoggerFactory.getLogger(CurrencyRateService.class);
    private static final String API_URL = "https://api.canlidoviz.com/web/items?marketId=1&type=0";

    private RestTemplate restTemplate;

    public CurrencyRateService(RestTemplate restTemplate){
        this.restTemplate = restTemplate;
    }

    public double getUSD(){
        ResponseEntity<CurrencyRatesItem[]> response = restTemplate.getForEntity(API_URL, CurrencyRatesItem[].class);

        if (response.getStatusCode() != HttpStatus.OK){
            log.error("Currency Rate Service getUSD Error Code => " + response.getStatusCodeValue());
            throw new DetectedException("Güncel dolar oranı çekilemedi. İşlemi daha sonra tekrar deneyin.");
        }else {
            for (CurrencyRatesItem item: response.getBody()) {
                if (item.getCode().equals("USD")){
                    return Double.valueOf(new DecimalFormat("##.###").format(item.getSellPrice()));
                }
            }
            log.error("Currency Rate Service getUSD Error => Itemlar çekildi fakat 'USD' kodlu item bulunamadı !");
            throw new DetectedException("Güncel dolar oranı çekilemedi. İşlemi daha sonra tekrar deneyin.");
        }
    }
}
