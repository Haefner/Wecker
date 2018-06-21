package hoschschulebochum.ContextAuswertung;

import junit.framework.Assert;

import org.junit.Test;

public class ContextTest {

    @Test
    public void getWeatherInformation() {
        Context context = new Context();
        String info = context.getWeatherInformation();
        Assert.assertTrue(info, info.contains("Bochum"));
    }
}