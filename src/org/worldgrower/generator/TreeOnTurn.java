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
package org.worldgrower.generator;

import org.worldgrower.Constants;
import org.worldgrower.OnTurn;
import org.worldgrower.World;
import org.worldgrower.WorldObject;
import org.worldgrower.actions.WoodPropertyUtils;
import org.worldgrower.condition.WorldStateChangedListeners;
import org.worldgrower.goal.DrownUtils;
import org.worldgrower.terrain.TerrainResource;
import org.worldgrower.terrain.TerrainType;

public class TreeOnTurn implements OnTurn {

	private static final int DEFAULT_WOOD_INCREASE = 4;
	
	@Override
	public void onTurn(WorldObject worldObject, World world, WorldStateChangedListeners creatureTypeChangedListeners) {
		if (worldObject.getProperty(Constants.CONDITIONS) == null) {
			throw new IllegalStateException("worldObject " + worldObject + " doesn't have conditions property");
		}
		
		increaseWoodAmount(worldObject, world);
		
		worldObject.getProperty(Constants.CONDITIONS).onTurn(worldObject, world, creatureTypeChangedListeners);
		DrownUtils.checkForDrowning(worldObject, world);
	}

	private static void increaseWoodAmount(WorldObject worldObject, World world) {
		int woodProduced = calculateWoodProduced(worldObject, world);
		if (!Constants.WOOD_PRODUCED.isAtMax(worldObject)) {
			worldObject.increment(Constants.WOOD_SOURCE, woodProduced);
		}
		worldObject.increment(Constants.WOOD_PRODUCED, woodProduced);
		WoodPropertyUtils.checkWoodSourceExhausted(worldObject);
		
		worldObject.setProperty(Constants.IMAGE_ID, TreeType.getTreeImageId(worldObject));
	}
	
	private static int calculateWoodProduced(WorldObject worldObject, World world) {
		int x = worldObject.getProperty(Constants.X);
		int y = worldObject.getProperty(Constants.Y);
		TerrainType terrainType = world.getTerrain().getTerrainInfo(x, y).getTerrainType();
		return DEFAULT_WOOD_INCREASE + terrainType.getBonus(TerrainResource.WOOD);
	}
	
	public static void increaseWoodAmountToMax(WorldObject worldObject, World world) {
		while (!Constants.WOOD_PRODUCED.isAtMax(worldObject)) {
			increaseWoodAmount(worldObject, world);
		}
	}
	
	public static String getPercentageWoodBonus(TerrainType terrainType) {
		return terrainType.getPercentageBonus(TerrainResource.WOOD, DEFAULT_WOOD_INCREASE);
	}
}
