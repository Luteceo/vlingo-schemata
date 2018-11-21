// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.schemata.model;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.vlingo.actors.Definition;
import io.vlingo.actors.testkit.TestActor;
import io.vlingo.actors.testkit.TestWorld;
import io.vlingo.lattice.model.sourcing.Sourced;
import io.vlingo.schemata.model.Events.SchemaVersionAssignedVersion;
import io.vlingo.schemata.model.Events.SchemaVersionDescribed;
import io.vlingo.schemata.model.Events.SchemaVersionSpecified;
import io.vlingo.schemata.model.Events.SchemaVersionPublished;
import io.vlingo.schemata.model.Events.SchemaVersionRemoved;
import io.vlingo.schemata.model.Id.ContextId;
import io.vlingo.schemata.model.Id.OrganizationId;
import io.vlingo.schemata.model.Id.SchemaId;
import io.vlingo.schemata.model.Id.SchemaVersionId;
import io.vlingo.schemata.model.Id.UnitId;

public class SchemaVersionEntityTest {
  private TestWorld world;
  private TestActor<SchemaVersion> schemaVersion;

  @Before
  public void setUp() throws Exception {
    world = TestWorld.start("schema-version-test");
    schemaVersion = world.actorFor(Definition.has(SchemaVersionEntity.class,
            Definition.parameters(
                    SchemaVersionId.uniqueFor(SchemaId.uniqueFor(ContextId.uniqueFor(UnitId.uniqueFor(OrganizationId.unique())))),
                    "description",
                    new SchemaVersion.Specification("specification"),
                    new SchemaVersion.Version("1.0.0"))),
            SchemaVersion.class);
  }

  @After
  public void tearDown() {
    world.terminate();
  }

  @Test
  public void testThatSchemaVersionIsSpecified() throws Exception {
    schemaVersion.actor().specifyWith(new SchemaVersion.Specification("new specification"));
    final SchemaVersionSpecified schemaVersionSpecified = (SchemaVersionSpecified) sourced().appliedEvent(1);
    Assert.assertEquals("new specification", schemaVersionSpecified.specification);
  }

  @Test
  public void testThatSchemaVersionIsDescribed() throws Exception {
    schemaVersion.actor().describeAs("new description");
    final SchemaVersionDescribed schemaVersionDescribed = (SchemaVersionDescribed) sourced().appliedEvent(1);
    Assert.assertEquals("new description", schemaVersionDescribed.description);

  }

  @Test
  public void testThatSchemaVersionPublishes() throws Exception {
    schemaVersion.actor().publish();
    final SchemaVersionPublished schemaVersionPublished = (SchemaVersionPublished) sourced().appliedEvent(1);
    Assert.assertNotNull(schemaVersionPublished);
  }

  @Test
  public void testThatSchemaVersionRemoves() throws Exception {
    schemaVersion.actor().remove();
    final SchemaVersionRemoved schemaVersionRemoved = (SchemaVersionRemoved) sourced().appliedEvent(1);
    Assert.assertNotNull(schemaVersionRemoved);
  }

  @Test
  public void testThatSchemaVersionAssignedVersion() throws Exception {
    schemaVersion.actor().assignVersionOf(new SchemaVersion.Version("version-1"));
    final SchemaVersionAssignedVersion schemaVersionAssignedVersion = (SchemaVersionAssignedVersion) sourced().appliedEvent(1);
    Assert.assertEquals("version-1", schemaVersionAssignedVersion.version);
  }

  @SuppressWarnings("unchecked")
  private <T> Sourced<T> sourced() {
    return (Sourced<T>) schemaVersion.actorInside();
  }
}