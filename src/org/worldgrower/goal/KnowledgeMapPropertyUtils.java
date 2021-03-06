/*******************************************************************************
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package org.worldgrower.goal;

import java.util.List;
import java.util.function.Function;

import org.worldgrower.Constants;
import org.worldgrower.Reach;
import org.worldgrower.World;
import org.worldgrower.WorldObject;
import org.worldgrower.attribute.IdProperty;
import org.worldgrower.attribute.ManagedProperty;

public class KnowledgeMapPropertyUtils {

	public static void everyoneInVicinityKnowsOfEvent(WorldObject performer, WorldObject target, World world) {
		List<WorldObject> peopleThatknow = getPeopleInVicinity(performer, world);
		for(WorldObject personThatknows : peopleThatknow) {
			personThatknows.getProperty(Constants.KNOWLEDGE_MAP).addKnowledge(target, world);
		}
	}
	
	public static void everyoneInVicinityKnowsOfProperty(WorldObject performer, WorldObject target, ManagedProperty<?> managedProperty, Object value, World world) {
		everyoneInVicinityKnowsOfProperty(performer, target, w -> true, managedProperty, value, world);
	}
	
	public static void everyoneInVicinityKnowsOfProperty(WorldObject performer, WorldObject target, Function<WorldObject, Boolean> testFunction, ManagedProperty<?> managedProperty, Object value, World world) {
		List<WorldObject> peopleThatknow = getPeopleInVicinity(performer, world);
		for(WorldObject personThatknows : peopleThatknow) {
			if (testFunction.apply(personThatknows)) {
				personThatknows.getProperty(Constants.KNOWLEDGE_MAP).addKnowledge(target, managedProperty, value);
			}
		}
	}

	private static List<WorldObject> getPeopleInVicinity(WorldObject performer, World world) {
		List<WorldObject> peopleThatknow = world.findWorldObjects(w -> w.hasIntelligence() && w.hasProperty(Constants.KNOWLEDGE_MAP) && Reach.distance(performer, w) < PerceptionPropertyUtils.calculateRadius(w, world));
		return peopleThatknow;
	}
	
	public static List<WorldObject> getWorldObjectsInVicinity(WorldObject performer, World world) {
		List<WorldObject> peopleThatknow = world.findWorldObjects(w -> Reach.distance(performer, w) < PerceptionPropertyUtils.calculateRadius(w, world));
		return peopleThatknow;
	}

	public static void peopleKnowOfProperty(List<WorldObject> people, WorldObject target, IdProperty managedProperty, Object value, World world) {
		for(WorldObject personThatknows : people) {
			personThatknows.getProperty(Constants.KNOWLEDGE_MAP).addKnowledge(target, managedProperty, value);
		}
	}	
}
