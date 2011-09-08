package pserver;

/**
Copyright 2009-2010 Igor Polevoy

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
import java.io.IOException;
import javax.servlet.*;
import activejdbc.Base;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * This is a filter for opening a connection before and closing connection after servlet.
 * Example of configuration:

 * <pre>

&lt;filter&gt;
&lt;filter-name&gt;activeJdbcFilter&lt;/filter-name&gt;
&lt;filter-class&gt;activejdbc.web.ActiveJdbcFilter&lt;/filter-class&gt;
&lt;init-param&gt;
&lt;param-name&gt;jndiName&lt;/param-name&gt;
&lt;param-value&gt;jdbc/test_jndi&lt;/param-value&gt;
&lt;/init-param&gt;
&lt;/filter&gt;
 * </pre>
 * @author Igor Polevoy
 */
public class ActiveJDBCFilter implements Filter {

    final Logger logger = LoggerFactory.getLogger(ActiveJDBCFilter.class);
    private static String jndiName;

    @Override
    public void init(FilterConfig config) throws ServletException {

        jndiName = config.getInitParameter("dbConnection");
        if (jndiName == null) {
            throw new IllegalArgumentException("must provide jndiName parameter for this filter");
        }
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
        long before = System.currentTimeMillis();
        try {
            Base.open(jndiName);
            Base.openTransaction();
            // continue
            chain.doFilter(req, resp);
            Base.commitTransaction();
        } catch (IOException e) {
            Base.rollbackTransaction();
            throw e;
        } catch (ServletException e) {
            Base.rollbackTransaction();
            throw e;
        } finally {

            Base.close();
        }
        double process = (System.currentTimeMillis() - before);
        System.out.println("Processing took: " + process + " milliseconds");
        //PicalagLog.log("processing_time|||" + ((HttpServletRequest)req).getPathInfo() + "|||" + process);
    }

    @Override
    public void destroy() {
    }
}
