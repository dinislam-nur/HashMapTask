package map;

import map.api.Map;
import map.impl.HashMap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HashMapTest {

    private static final Object OBJECT_KEY = new Object();
    private static final Object OBJECT_VALUE = new Object();

    private static final Map map = new HashMap();

    @BeforeAll
    static void init() {
        map.put(OBJECT_KEY, OBJECT_VALUE);
        map.put("first", 1);
        map.put("second", 2);
        map.put("removeKey", "removeValue");
        map.put("forUpdate", "beforeUpdateValue");
    }

    @Test
    @DisplayName("Check the size functionality")
    void SizeTest() {
        //Prepare new map
        final Map hashMap = new HashMap();
        hashMap.put("first", "1");
        hashMap.put("second", "2");

        //Assertion
        assertEquals(2, hashMap.size());
        hashMap.remove("first");
        assertEquals(1, hashMap.size());
    }

    @Nested
    @DisplayName("Checking the update of value")
    class UpdateTest{

        @Test
        @DisplayName("for String Key")
        void updateTestForStringKey() {
            assertEquals("beforeUpdateValue", map.get("forUpdate"));
            map.put("forUpdate", "UpdatedValue");
            assertEquals("UpdatedValue", map.get("forUpdate"));
        }

        @Test
        @DisplayName("for Object Key")
        void updateTestForObjectKey() {

            //Prepare new local Object value
            final Object newObjectValue = new Object();

            //Assertion
            assertEquals(OBJECT_VALUE, map.get(OBJECT_KEY));
            map.put(OBJECT_KEY, newObjectValue);
            assertEquals(newObjectValue, map.get(OBJECT_KEY));
            map.put(OBJECT_KEY, OBJECT_VALUE);
        }
    }

    @Nested
    @DisplayName("Checking the get functionality")
    class GetTest {

        @Test
        @DisplayName("for String Keys")
        void getTestForStringKey() {
            assertAll(
                    () -> assertEquals(1, map.get("first")),
                    () -> assertEquals(2, map.get("second"))
            );
        }

        @Test
        @DisplayName("for Object Key and Value")
        void getTestForObjectKeyAndValue() {
            assertEquals(OBJECT_VALUE, map.get(OBJECT_KEY));
        }

        @Test
        @DisplayName("for Non-HashMap element")
        void getTestForNonHashMapElement() {
            assertNull(map.get("Non-HashMap element"));
        }
    }


    @Nested
    @DisplayName("Checking content of Map")
    class ContainsTest {

        @Test
        @DisplayName("for String Key")
        void containsTestForStringKey() {
            assertTrue(map.contains("first"));
        }

        @Test
        @DisplayName("for Object Key")
        void containsTestForObjectKey() {
            assertTrue(map.contains(OBJECT_KEY));
        }

        @Test
        @DisplayName("for Non-HashMap element")
        void containsTestForNonHashMapElement() {
            assertFalse(map.contains("Non-HashMap element"));
        }
    }

    @Nested
    @DisplayName("Checking the delete function")
    class RemoveTest {

        @Test
        @DisplayName("for entry with String Key")
        void removeTestForEntryWithStringKey() {
            assertTrue(map.contains("removeKey"));
            assertEquals("removeValue", map.remove("removeKey"));
            assertFalse(map.contains("removeKey"));
            map.put("removeKey", "removeValue");
        }

        @Test
        @DisplayName("for entry with Object Key")
        void removeTestForEntryWithObjectKey() {
            assertTrue(map.contains(OBJECT_KEY));
            map.remove(OBJECT_KEY);
            assertFalse(map.contains(OBJECT_KEY));
            map.put(OBJECT_KEY, OBJECT_VALUE);
        }

        @Test
        @DisplayName("for Non-HashMap element")
        void removeNonHashMapElementTest() {
            assertNull(map.remove("Non-HashMap element"));
        }
    }

    @Test
    @DisplayName("Checking functionality of an empty Map")
    void anEmptyHashMapTest() {
        //Prepare empty HashMap
        final Map emptyMap = new HashMap();

        //Assertion
        assertAll(
                () -> assertNull(emptyMap.get(OBJECT_KEY)),
                () -> assertFalse(emptyMap.contains(OBJECT_KEY))
        );
    }

    @Test
    @DisplayName("Checking the compare functionality")
    void equalsTest() {
        //Preparing a pair Maps that have the same entries
        final Object firstKey = new Object();
        final Object secondKey = new Object();
        final Object firstValue = new Object();
        final Object secondValue = new Object();

        final Map leftMap = new HashMap();
        final Map rightMap = new HashMap();

        leftMap.put(firstKey, firstValue);
        leftMap.put(secondKey, secondValue);
        rightMap.put(secondKey, secondValue);
        rightMap.put(firstKey, firstValue);

        //Assertion
        assertAll(
                () -> assertEquals(rightMap, leftMap),
                () -> assertNotEquals(map, leftMap)
        );
    }

    @Test
    @DisplayName("Check the ability to add null key and null value")
    void nullKeyAndNullValueTest() {
        //Prepare data
        final Object key = null;
        final Object value = null;
        map.put(key, value);

        //Assertion
        assertNull(map.get(key));
    }

    @Nested
    @DisplayName("Check the ability to throw exceptions")
    class ExceptionTests {

        @Test
        @DisplayName("when we pass invalid capacity into constructor")
        void invalidCapacityTest() {
            assertThrows(IllegalArgumentException.class, () -> new HashMap(-1));
        }

        @Test
        @DisplayName("when we pass invalid load factor into constructor")
        void invalidLoadFactoryTest() {
            assertAll(
                    () -> assertThrows(IllegalArgumentException.class, () -> new HashMap(16, 1.1)),
                    () -> assertThrows(IllegalArgumentException.class, () -> new HashMap(16, -0.1))
            );

        }
    }
}