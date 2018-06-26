package uk.gov.pay.directdebit.payments.links;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Objects;

import static com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class Link {

    private String href;
    private String method;
    private String rel;
    
    private Link(String href, String method, String rel) {
        this.href = href;
        this.method = method;
        this.rel = rel;
    }

    public Link() {}

    public String getHref() {
        return href;
    }

    public String getMethod() {
        return method;
    }

    public String getRel() { return rel; }

    public static Link ofValue(String href, String method, String rel) {
        return new Link(href, method, rel);
    }

    @Override
    public String toString() {
        return "Link{" +
                "rel='" + rel + '\'' +
                ", href='" + href + '\'' +
                ", method='" + method + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Link link = (Link) o;
        return Objects.equals(href, link.href) &&
                Objects.equals(method, link.method);
    }

    @Override
    public int hashCode() {
        return Objects.hash(href, method);
    }
}
