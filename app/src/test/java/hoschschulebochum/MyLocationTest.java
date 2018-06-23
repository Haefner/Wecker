package hoschschulebochum;

import junit.framework.Assert;

import org.junit.Test;

public class MyLocationTest {

    @Test
    public void doesLocationFit() {
        // https://www.latlong.net/
        MyLocation home = new MyLocation("Hochschule Bochum" , 51.447709, 7.270900);

        //Hochschule Bochum
        Assert.assertTrue(home.doesLocationFit(51.446905, 7.271703));

        //Wasserstraße Bochum
        Assert.assertFalse(home.doesLocationFit(51.463159, 7.234362));

    }
}