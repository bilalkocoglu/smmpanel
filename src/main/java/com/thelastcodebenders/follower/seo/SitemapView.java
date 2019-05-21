package com.thelastcodebenders.follower.seo;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.view.AbstractView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Writer;
import java.util.Map;

@Component
public final class SitemapView extends AbstractView {
    private final SitemapService sitemapService;

    public SitemapView (SitemapService sitemapService){
        this.sitemapService = sitemapService;
    }

    @Override
    protected void renderMergedOutputModel(Map<String, Object> model,
                                           HttpServletRequest request,
                                           HttpServletResponse response) throws Exception {
        response.setContentType(MediaType.APPLICATION_XML_VALUE);

        try (Writer writer = response.getWriter()){
            writer.append(sitemapService.createSitemap());
        }
    }
}
