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
package org.worldgrower.deity;

import java.io.ObjectStreamException;
import java.util.Arrays;
import java.util.List;

import org.worldgrower.Constants;
import org.worldgrower.World;
import org.worldgrower.WorldObject;
import org.worldgrower.attribute.SkillProperty;
import org.worldgrower.condition.Condition;
import org.worldgrower.condition.WorldStateChangedListeners;
import org.worldgrower.goal.Goals;
import org.worldgrower.gui.ImageIds;
import org.worldgrower.personality.PersonalityTrait;
import org.worldgrower.profession.Professions;

public class Hera implements Deity {

	@Override
	public String getName() {
		return "Hera";
	}

	@Override
	public String getExplanation() {
		return getName() + " is the Goddess of women and marriage";
	}

	public Object readResolve() throws ObjectStreamException {
		return readResolveImpl();
	}
	
	@Override
	public List<String> getReasons() {
		return Arrays.asList(
				"As a priest of " + getName() + ", I want to honor the queen of the Gods",
				"I worship " + getName() + " because I value family life"
		);
	}

	@Override
	public int getReasonIndex(WorldObject performer, World world) {
		boolean hasChildren = performer.getProperty(Constants.CHILDREN).size() > 0;
		boolean hasMate = performer.getProperty(Constants.MATE_ID) != null;
		if (performer.getProperty(Constants.PROFESSION) == Professions.PRIEST_PROFESSION) {
			return 0;
		} else if (hasChildren && !hasMate) {
			return 1;
		}
		
		return -1;
	}
	
	@Override
	public int getOrganizationGoalIndex(WorldObject performer, World world) {
		if (performer.getProperty(Constants.PERSONALITY).getValue(PersonalityTrait.POWER_HUNGRY) > 0) {
			return getOrganizationGoals().indexOf(Goals.SWITCH_DEITY_GOAL);
		}
		return -1;
	}
	
	@Override
	public void onTurn(World world, WorldStateChangedListeners creatureTypeChangedListeners) {
	}

	@Override
	public ImageIds getStatueImageId() {
		return ImageIds.STATUE_OF_HERA;
	}
	
	@Override
	public SkillProperty getSkill() {
		return Constants.INSIGHT_SKILL;
	}
	
	@Override
	public ImageIds getBoonImageId() {
		return ImageIds.HERA_SYMBOL;
	}
	
	@Override
	public Condition getBoon() {
		return Condition.HERA_BOON_CONDITION;
	}
	
	@Override
	public String getBoonDescription() {
		return getName() + "'s boon: grants a bonus in seeing through illusions and disguises";
	}
}
