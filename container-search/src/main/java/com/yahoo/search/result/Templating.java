// Copyright 2017 Yahoo Holdings. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
package com.yahoo.search.result;

import java.util.Map;

import com.yahoo.container.jdisc.HttpRequest;
import com.yahoo.prelude.templates.SearchRendererAdaptor;
import com.yahoo.prelude.templates.TemplateSet;
import com.yahoo.prelude.templates.UserTemplate;
import com.yahoo.processing.rendering.Renderer;
import com.yahoo.search.Result;
import com.yahoo.search.query.Presentation;

/**
 * Helper methods and data store for result attributes geared towards result
 * rendering and presentation.
 *
 * @author Steinar Knutsen
 * @deprecated do not use
 */
@Deprecated // OK (But wait for deprecated handlers in vespaclient-container-plugin to be removed)
// TODO: Remove on Vespa 7
public class Templating {

    private final Result result;
    private Renderer<Result> renderer;

    public Templating(Result result) {
        super();
        this.result = result;
    }

    /**
     * Returns The first hit presented in the result as an index into the global
     * list of all hits generated by the user query.
     */
    public int getFirstHitNo() {
        return result.getQuery().getOffset() + 1;
    }

    /**
     * Returns the first hit of the next result page, 0 if there aren't any more
     * hits available
     */
    public long getNextFirstHitNo() {
        if (result.getQuery().getHits() > result.getConcreteHitCount()) {
            return 0;
        }

        return Math.min(getLastHitNo() + 1, result.getTotalHitCount());
    }

    /**
     * Returns the first hit of the next result page, 0 if there aren't any more
     * hits available
     */
    public long getNextLastHitNo() {
        if (result.getQuery().getHits() > result.getConcreteHitCount()) {
            return 0;
        }

        return Math.min(getLastHitNo() + result.getConcreteHitCount(), result.getTotalHitCount());
    }

    /**
     * Returns the number of the last result of the current hit page.
     */
    public int getLastHitNo() {
        return getFirstHitNo() + result.getConcreteHitCount() - 1;
    }

    /**
     * The first hit presented on the previous result page as an index into the
     * global list of all hits generated by the user query
     */
    public int getPrevFirstHitNo() {
        return Math.max(getFirstHitNo() - result.getQuery().getHits(), 1);
    }

    /**
     * The last hit presented on the previous result page as an index into the
     * global list of all hits generated by the user query
     */
    public int getPrevLastHitNo() {
        return Math.max(getFirstHitNo() - 1, 0);
    }

    /**
     * An URL that may be used to obtain the next result page.
     */
    public String getNextResultURL() {
        HttpRequest request = result.getQuery().getHttpRequest();
        StringBuilder nextURL = new StringBuilder();

        nextURL.append(getPath(request)).append("?");
        parametersExceptOffset(request, nextURL);

        int offset = getLastHitNo();

        nextURL.append("&").append("offset=").append(Integer.toString(offset));
        return nextURL.toString();
    }

    /**
     * An URL that may be used to obtain the previous result page.
     */
    public String getPreviousResultURL() {
        HttpRequest request = result.getQuery().getHttpRequest();
        StringBuilder prevURL = new StringBuilder();

        prevURL.append(getPath(request)).append("?");
        parametersExceptOffset(request, prevURL);
        int offset = getPrevFirstHitNo() - 1;
        prevURL.append("&").append("offset=").append(Integer.toString(offset));
        return prevURL.toString();
    }

    public String getCurrentResultURL() {
        HttpRequest request = result.getQuery().getHttpRequest();
        StringBuilder thisURL = new StringBuilder();

        thisURL.append(getPath(request)).append("?");
        parameters(request, thisURL);
        return thisURL.toString();
    }

    private String getPath(HttpRequest request) {
        String path = request.getUri().getPath();
        if (path == null) {
            path = "";
        }
        return path;
    }

    private void parametersExceptOffset(HttpRequest request, StringBuilder nextURL) {
        int startLength = nextURL.length();
        for (Map.Entry<String, String> property : request.propertyMap().entrySet()) {
            if (property.getKey().equals("offset")) continue;

            if (nextURL.length() > startLength)
                nextURL.append("&");
            nextURL.append(property.getKey()).append("=").append(property.getValue());
        }
    }

    private void parameters(HttpRequest request, StringBuilder nextURL) {
        int startLength = nextURL.length();
        for (Map.Entry<String, String> property : request.propertyMap().entrySet()) {
            if (nextURL.length() > startLength)
                nextURL.append("&");
            nextURL.append(property.getKey()).append("=").append(property.getValue());
        }
    }

    /**
     * Returns the templates which will render the result. This is never null.
     * If default rendering is used, it is a TemplateSet containing no
     * templates.
     *
     * @deprecated use a renderer instead
     */
    @SuppressWarnings("rawtypes")
    // TODO: Remove on Vespa 7
    @Deprecated // OK
    public UserTemplate getTemplates() {
        if (renderer == null) {
            return TemplateSet.getDefault();
        } else if (renderer instanceof SearchRendererAdaptor) {
            return ((SearchRendererAdaptor) renderer).getAdaptee();
        } else {
            throw new RuntimeException(
                    "Please use getTemplate() instead of getTemplates() when using the new template api.");
        }
    }

    /**
     * Sets the template set which should render this result set
     *
     * @param templates
     *                the templates which should render this result, or null to
     *                use the default xml rendering
     */
    @SuppressWarnings("deprecation")
    public void setTemplates(@SuppressWarnings("rawtypes") UserTemplate templates) {
        if (templates == null) {
            setTemplates(TemplateSet.getDefault());
        } else {
            setRenderer(new SearchRendererAdaptor(templates));
        }
    }

    /**
     * @deprecated since 5.1.21, use {@link Presentation#getRenderer()}
     */
    public Renderer<Result> getRenderer() {
        return renderer;
    }

    /**
     * @deprecated since 5.1.21, use {@link Presentation#setRenderer(com.yahoo.component.ComponentSpecification)}
     */
    public void setRenderer(Renderer<Result> renderer) {
        this.renderer = renderer;
    }

    /**
     * For internal use only.
     */
    public boolean usesDefaultTemplate() {
        return renderer == null ||
                (renderer instanceof SearchRendererAdaptor &&
                ((SearchRendererAdaptor) renderer).getAdaptee().isDefaultTemplateSet());
    }

}