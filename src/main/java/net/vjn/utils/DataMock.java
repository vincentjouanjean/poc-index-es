package net.vjn.utils;

import net.vjn.entity.Object1;
import net.vjn.entity.Object2;
import net.vjn.entity.ParentObject;
import org.apache.commons.lang3.RandomStringUtils;

public final class DataMock {
    public static ParentObject getObjects(final int id, final int nbObject) {
        final ParentObject s = new ParentObject();
        s.setId(id);
        s.setObj1(new Object1("1", "2", "3"));
        s.setObj2(getObj2(nbObject));
        return s;
    }

    public static Object2 getObj2(final int i) {
        if (i > 0) {
            return new Object2(randomString(), randomString(), randomString(), randomString(), randomString(), randomString(), randomString(), randomString(), randomString(), randomString(), getObj2(i - 1));
        } else {
            return null;
        }
    }

    private static String randomString() {
        return RandomStringUtils.random(100, true, true);
    }
}
