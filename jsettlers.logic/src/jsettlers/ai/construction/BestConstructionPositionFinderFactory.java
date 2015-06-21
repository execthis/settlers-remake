/**
 * ****************************************************************************
 * Copyright (c) 2015
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 * *****************************************************************************
 */
package jsettlers.ai.construction;

import static jsettlers.common.landscape.EResourceType.COAL;
import static jsettlers.common.landscape.EResourceType.GOLD;
import static jsettlers.common.landscape.EResourceType.IRON;
import jsettlers.common.buildings.EBuildingType;

public class BestConstructionPositionFinderFactory {

	public final IBestConstructionPositionFinder getBestConstructionPositionFinderFor(EBuildingType type) {
		switch (type) {
		case STONECUTTER:
			return new BestStoneCutterConstructionPositionFinder(type);
		case LUMBERJACK:
			return new BestLumberJackConstructionPositionFinder(type);
		case FORESTER:
			return new BestForesterConstructionPositionFinder(type);
		case SAWMILL:
			return new BestSawMillConstructionPositionFinder(type);
		case TOWER:
		case BIG_TOWER:
		case CASTLE:
			return new BestMilitaryConstructionPositionFinder(type);
		case FARM:
		case WINEGROWER:
			return new BestFarmAndWineGrowerConstructionPositionFinder(type);
		case COALMINE:
			return new BestMineConstructionPositionFinder(type, COAL);
		case IRONMINE:
			return new BestMineConstructionPositionFinder(type, IRON);
		case GOLDMINE:
			return new BestMineConstructionPositionFinder(type, GOLD);
		default:
			return new NearDiggersConstructionPositionFinder(type);
		}
	}

}
