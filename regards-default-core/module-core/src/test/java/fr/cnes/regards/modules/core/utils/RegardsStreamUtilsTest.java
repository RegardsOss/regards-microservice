/*
 * Copyright 2017 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
 *
 * This file is part of REGARDS.
 *
 * REGARDS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * REGARDS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with REGARDS. If not, see <http://www.gnu.org/licenses/>.
 */
package fr.cnes.regards.modules.core.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Class RegardsStreamUtilsTest
 *
 * Test for {@link RegardsStreamUtils}
 *
 * @author xbrochar
 */
public class RegardsStreamUtilsTest {

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
    }

    /**
     * Check that the method filters out duplicates on a specific field, keeping only the first seen element. We will
     * here remove duplicates from a Person collection by looking only to the name.
     */
    @Test
    public void distinctByKey() {
        final List<Person> people = new ArrayList<>();
        final Person person0 = new Person("Alice");
        final Person person1 = new Person("Bob");
        final Person person2 = new Person("Alice");
        people.add(person0);
        people.add(person1);
        people.add(person2);

        // Define the expected filtered collection.
        // We expect to filter out person2, because its name is a duplicate of person0.
        // Remember, the first seen element is always kept, so here it is person0.
        final List<Person> expected = new ArrayList<>();
        expected.add(person0);
        expected.add(person1);

        // Actual result
        final Predicate<Person> filter = RegardsStreamUtils.distinctByKey(p -> p.getName());
        final List<Person> actual = people.stream().filter(filter).collect(Collectors.toList());

        // Compare
        Assert.assertEquals(expected, actual);
    }

    /**
     * Simple POJO representing a person. Add getters and setters if needed.
     *
     * @author xbrochar
     */
    private class Person {

        /**
         * An other field
         */
        private final String name;

        /**
         * @param pAge
         * @param pName
         */
        public Person(final String pName) {
            name = pName;
        }

        /**
         * @return the name
         */
        public String getName() {
            return name;
        }
    }

}
