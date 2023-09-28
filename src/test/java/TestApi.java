import cn.paper_card.MojangProfileApi;
import org.junit.Assert;
import org.junit.Test;

import java.util.UUID;

public class TestApi {

    @Test
    public void testByName() {
        final MojangProfileApi mojangProfileApi = new MojangProfileApi();

        final MojangProfileApi.Profile profile;

        try {
            profile = mojangProfileApi.requestByName("Paper99");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Assert.assertEquals(profile.uuid().toString(), "20554467-84cb-4773-a084-e3cfa867d480");
    }

    @Test
    public void testByUuid() {

        final MojangProfileApi.Profile profile;
        try {
            profile = new MojangProfileApi().requestByUuid(UUID.fromString("20554467-84cb-4773-a084-e3cfa867d480"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Assert.assertEquals("Paper99", profile.name());

    }
}
