// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.schemata.infra.persistence;

import io.vlingo.actors.World;
import io.vlingo.actors.testkit.AccessSafely;
import io.vlingo.actors.testkit.TestUntil;
import io.vlingo.common.Outcome;
import io.vlingo.lattice.model.object.ObjectTypeRegistry;
import io.vlingo.schemata.NoopDispatcher;
import io.vlingo.schemata.model.ContextState;
import io.vlingo.schemata.model.Id.OrganizationId;
import io.vlingo.schemata.model.Id.UnitId;
import io.vlingo.schemata.model.Id.ContextId;
import io.vlingo.schemata.model.OrganizationState;
import io.vlingo.schemata.model.UnitState;
import io.vlingo.symbio.store.DataFormat;
import io.vlingo.symbio.store.Result;
import io.vlingo.symbio.store.StorageException;
import io.vlingo.symbio.store.common.jdbc.Configuration;
import io.vlingo.symbio.store.common.jdbc.hsqldb.HSQLDBConfigurationProvider;
import io.vlingo.symbio.store.object.ListQueryExpression;
import io.vlingo.symbio.store.object.MapQueryExpression;
import io.vlingo.symbio.store.object.ObjectStore;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class SchemataObjectStoreTest {

    private NoopDispatcher dispatcher;
    private ObjectStore objectStore;
    private World world;

    @Test
    public void testThatObjectStoreConnects() {
        assertNotNull(objectStore);
    }

    @Test
    public void testThatObjectStoreInsertsOrganizationStateAndQuerys() {
        final TestPersistResultInterest persistInterest = new TestPersistResultInterest();
        final AccessSafely access = persistInterest.afterCompleting(1);
        final OrganizationState organizationState = OrganizationState.from(
                1L,
                OrganizationId.existing("A343"),
                "Vlingo", "Organization Vlingo");
        objectStore.persist(organizationState, persistInterest);
        final Outcome<StorageException, Result> outcome = access.readFrom("outcome");
        assertEquals(Result.Success, outcome.andThen(success -> success).get());

        final TestQueryResultInterest queryInterest = new TestQueryResultInterest();

        // Map
        queryInterest.until = TestUntil.happenings(1);
        querySelect(queryInterest, OrganizationState.class, "ORGANIZATION");
        queryInterest.until.completes();
        assertNotNull(queryInterest.singleResult.get());
        final OrganizationState insertedOrganizationState = (OrganizationState) queryInterest.singleResult.get().stateObject;
        assertEquals(organizationState, insertedOrganizationState);

        // List
        queryInterest.until = TestUntil.happenings(1);
        objectStore.queryObject(
                ListQueryExpression.using(
                        OrganizationState.class,
                        "SELECT * FROM ORGANIZATION WHERE id = <listArgValues>",
                        Arrays.asList(1L)),
                queryInterest);
        queryInterest.until.completes();
        assertNotNull(queryInterest.singleResult.get());
        assertEquals(organizationState, queryInterest.singleResult.get().stateObject);

        // update
        queryInterest.until = TestUntil.happenings(1);
        final OrganizationState updatedOrganizationState =
                insertedOrganizationState.define("VlingoV2", "Organization Vlingo V2");

        objectStore.persist(updatedOrganizationState, persistInterest);
        querySelect(queryInterest, OrganizationState.class, "ORGANIZATION");

        queryInterest.until.completes();
        assertNotNull(queryInterest.singleResult.get());
        assertEquals(updatedOrganizationState, queryInterest.singleResult.get().stateObject);
    }

    @Test
    public void testThatObjectStoreInsertsUnitStateAndQuerys() {
        final TestPersistResultInterest persistInterest = new TestPersistResultInterest();
        final AccessSafely access = persistInterest.afterCompleting(1);
        final UnitState unitState = UnitState.from(
                1L,
                UnitId.existing("A343:U44"),
                "Vlingo", "Unit Vlingo");
        objectStore.persist(unitState, persistInterest);
        final Outcome<StorageException, Result> outcome = access.readFrom("outcome");
        assertEquals(Result.Success, outcome.andThen(success -> success).get());

        final TestQueryResultInterest queryInterest = new TestQueryResultInterest();

        queryInterest.until = TestUntil.happenings(1);
        querySelect(queryInterest, UnitState.class,"UNIT");
        queryInterest.until.completes();
        assertNotNull(queryInterest.singleResult.get());
        final UnitState insertedUnitState = (UnitState) queryInterest.singleResult.get().stateObject;
        assertEquals(unitState, insertedUnitState);

        // update
        queryInterest.until = TestUntil.happenings(1);
        final UnitState updatedUnitState =
                insertedUnitState.defineWith("VlingoV2", "Unit Vlingo V2");

        objectStore.persist(updatedUnitState, persistInterest);
        querySelect(queryInterest, UnitState.class, "UNIT");

        queryInterest.until.completes();
        assertNotNull(queryInterest.singleResult.get());
        assertEquals(updatedUnitState, queryInterest.singleResult.get().stateObject);
    }

    @Test
    public void testThatObjectStoreInsertsContextStateAndQuerys() {
        final TestPersistResultInterest persistInterest = new TestPersistResultInterest();
        final AccessSafely access = persistInterest.afterCompleting(1);
        final ContextState contextState = ContextState.from(
                1L,
                ContextId.existing("A343:U44:C13"),
                "io.vlingo", "Context Vlingo");
        objectStore.persist(contextState, persistInterest);
        final Outcome<StorageException, Result> outcome = access.readFrom("outcome");
        assertEquals(Result.Success, outcome.andThen(success -> success).get());

        final TestQueryResultInterest queryInterest = new TestQueryResultInterest();

        queryInterest.until = TestUntil.happenings(1);
        querySelect(queryInterest, ContextState.class,"CONTEXT");
        queryInterest.until.completes();
        assertNotNull(queryInterest.singleResult.get());
        final ContextState insertedContextState = (ContextState) queryInterest.singleResult.get().stateObject;
        assertEquals(contextState, insertedContextState);

        // update
        queryInterest.until = TestUntil.happenings(1);
        final ContextState updatedContextState =
                insertedContextState.define("io.vlingoV2", "Context Vlingo V2");

        objectStore.persist(updatedContextState, persistInterest);
        querySelect(queryInterest, ContextState.class, "CONTEXT");

        queryInterest.until.completes();
        assertNotNull(queryInterest.singleResult.get());
        assertEquals(updatedContextState, queryInterest.singleResult.get().stateObject);
    }

    @Before
    public void setUp() throws Exception {
        world = World.startWithDefaults("test-store");

        dispatcher = new NoopDispatcher();

        final Configuration configuration = HSQLDBConfigurationProvider.testConfiguration(DataFormat.Native);
        final SchemataObjectStore schemataObjectStore = new SchemataObjectStore(configuration);
        objectStore = schemataObjectStore.objectStoreFor(world, dispatcher, schemataObjectStore.persistentMappers());
        final ObjectTypeRegistry registry = new ObjectTypeRegistry(world);
        schemataObjectStore.register(registry, objectStore);
    }

    @After
    public void tearDown() {
        objectStore.close();
        world.terminate();
    }

    private void querySelect(final TestQueryResultInterest queryInterest, Class<?> type, final String name) {
        objectStore.queryObject(
                MapQueryExpression.using(
                        type,
                        "SELECT * FROM " + name + " WHERE id = :id",
                        MapQueryExpression.map("id", 1L)),
                queryInterest);
    }
}
