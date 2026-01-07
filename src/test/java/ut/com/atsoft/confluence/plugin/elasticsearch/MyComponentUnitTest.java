package ut.com.atsoft.confluence.plugin.elasticsearch;

import org.junit.Test;
import com.atsoft.confluence.plugin.elasticsearch.api.MyPluginComponent;
import com.atsoft.confluence.plugin.elasticsearch.impl.MyPluginComponentImpl;

import static org.junit.Assert.assertEquals;

public class MyComponentUnitTest {
    @Test
    public void testMyName() {
        MyPluginComponent component = new MyPluginComponentImpl(null);
        assertEquals("names do not match!", "myComponent", component.getName());
    }
}