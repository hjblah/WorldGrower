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
package org.worldgrower.actions.magic;

import java.io.ObjectStreamException;
import java.util.List;

import org.worldgrower.ArgumentRange;
import org.worldgrower.Constants;
import org.worldgrower.World;
import org.worldgrower.WorldObject;
import org.worldgrower.actions.AttackUtils;
import org.worldgrower.actions.DeadlyAction;
import org.worldgrower.attribute.SkillProperty;
import org.worldgrower.attribute.SkillUtils;
import org.worldgrower.goal.LocationUtils;
import org.worldgrower.terrain.TerrainType;

public class LightningBoltAttackAction implements MagicSpell, DeadlyAction {

	private static final int ENERGY_USE = 600;
	
	@Override
	public void execute(WorldObject performer, WorldObject target, int[] args, World world) {
		AttackUtils.magicAttack(5, this, performer, target, args, world, SkillUtils.getSkillBonus(performer, getSkill()));
	
		int targetX = target.getProperty(Constants.X);
		int targetY = target.getProperty(Constants.Y);
		TerrainType terrainType = world.getTerrain().getTerrainInfo(targetX, targetY).getTerrainType();
		
		if (terrainType == TerrainType.WATER) {
			List<WorldObject> worldObjectsInSurroundingWater = LocationUtils.findWorldObjectsInSurroundingWater(targetX, targetY, world);
			for(WorldObject worldObjectInSurroundingWater : worldObjectsInSurroundingWater) {
				AttackUtils.magicAttack(5, this, performer, worldObjectInSurroundingWater, args, world, SkillUtils.getSkillBonus(performer, getSkill()));
			}
		}
		
		SkillUtils.useEnergy(performer, getSkill(), ENERGY_USE);
	}
	
	@Override
	public boolean isValidTarget(WorldObject performer, WorldObject target, World world) {
		return ((target.hasProperty(Constants.ARMOR)) && (target.getProperty(Constants.HIT_POINTS) > 0) && performer.getProperty(Constants.KNOWN_SPELLS).contains(this));
	}

	@Override
	public int distance(WorldObject performer, WorldObject target, int[] args, World world) {
		return AttackUtils.distanceWithFreeLeftHand(performer, target, 4)
				+ SkillUtils.distanceForEnergyUse(performer, getSkill(), ENERGY_USE);
	}
	
	@Override
	public ArgumentRange[] getArgumentRanges() {
		return ArgumentRange.EMPTY_ARGUMENT_RANGE;
	}
	
	@Override
	public String getDescription(WorldObject performer, WorldObject target, int[] args, World world) {
		return "attacking " + target.getProperty(Constants.NAME);
	}

	@Override
	public String getSimpleDescription() {
		return "lightning bolt";
	}
	
	public Object readResolve() throws ObjectStreamException {
		return readResolveImpl();
	}

	@Override
	public int getResearchCost() {
		return 40;
	}

	@Override
	public SkillProperty getSkill() {
		return Constants.EVOCATION_SKILL;
	}

	@Override
	public int getRequiredSkillLevel() {
		return 2;
	}

	@Override
	public String getDeathDescription(WorldObject performer, WorldObject target) {
		return "electrocuted to death";
	}
	
	public boolean hasRequiredEnergy(WorldObject performer) {
		return performer.getProperty(Constants.ENERGY) >= ENERGY_USE;
	}

	@Override
	public String getDescription() {
		return "deals damage to target and other targets if something conducts the electricity";
	}
}
