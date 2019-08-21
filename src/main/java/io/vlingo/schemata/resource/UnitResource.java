// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.schemata.resource;

import static io.vlingo.common.serialization.JsonSerialization.serialized;
import static io.vlingo.http.Response.Status.Conflict;
import static io.vlingo.http.Response.Status.Created;
import static io.vlingo.http.Response.Status.Ok;
import static io.vlingo.http.ResponseHeader.Location;
import static io.vlingo.http.ResponseHeader.headers;
import static io.vlingo.http.ResponseHeader.of;
import static io.vlingo.schemata.Schemata.NoId;
import static io.vlingo.schemata.Schemata.StageName;
import static io.vlingo.schemata.Schemata.UnitsPath;

import io.vlingo.actors.Stage;
import io.vlingo.actors.World;
import io.vlingo.common.Completes;
import io.vlingo.http.Header.Headers;
import io.vlingo.http.Response;
import io.vlingo.http.ResponseHeader;
import io.vlingo.http.resource.ResourceHandler;
import io.vlingo.schemata.model.Id.OrganizationId;
import io.vlingo.schemata.model.Id.UnitId;
import io.vlingo.schemata.model.Unit;
import io.vlingo.schemata.resource.data.UnitData;

public class UnitResource extends ResourceHandler {
  private final UnitCommands commands;
  private final Stage stage;

  public UnitResource(final World world) {
    this.stage = world.stageNamed(StageName);
    this.commands = new UnitCommands(this.stage, 10);
  }

  public Completes<Response> defineWith(final String organizationId, final String name, final String description) {
    return Unit.with(stage, OrganizationId.existing(organizationId), name, description)
            .andThenTo(state -> {
                final String location = unitLocation(state.unitId);
                final Headers<ResponseHeader> headers = headers(of(Location, location));
                final String serialized = serialized(UnitData.from(state));

                return Completes.withSuccess(Response.of(Created, headers, serialized));
              })
            .otherwise(response -> Response.of(Conflict, serialized(UnitData.from(NoId, NoId, name, description))));
  }

  public Completes<Response> describeAs(final String organizationId, final String unitId, final String description) {
    return commands
              .describeAs(UnitId.existing(organizationId, unitId), description).answer()
              .andThenTo(state -> Completes.withSuccess(Response.of(Ok, serialized(UnitData.from(state)))));
  }

  public Completes<Response> renameTo(final String organizationId, final String unitId, final String name) {
    return commands
            .renameTo(UnitId.existing(organizationId, unitId), name).answer()
            .andThenTo(state -> Completes.withSuccess(Response.of(Ok, serialized(UnitData.from(state)))));
  }

  private String unitLocation(final UnitId unitId) {
    return String.format(UnitsPath, unitId.organizationId.value, unitId.value);
  }
}
