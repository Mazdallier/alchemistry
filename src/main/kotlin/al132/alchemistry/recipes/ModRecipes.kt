package al132.alchemistry.recipes

import al132.alchemistry.chemistry.CompoundRegistry
import al132.alchemistry.chemistry.ElementRegistry
import al132.alchemistry.items.ModItems
import al132.alchemistry.utils.toCompoundStack
import al132.alchemistry.utils.toElementStack
import al132.alib.utils.Utils.firstOre
import al132.alib.utils.Utils.oreExists
import al132.alib.utils.extensions.toDict
import al132.alib.utils.extensions.toStack
import net.minecraft.block.BlockTallGrass
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fluids.FluidRegistry
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fml.common.IFuelHandler
import net.minecraftforge.fml.common.registry.GameRegistry
import net.minecraftforge.oredict.OreDictionary
import java.util.*

/**
 * Created by al132 on 1/16/2017.
 */

data class DissolverOreData(val prefix: String, val quantity: Int, val strs: List<String>) {
    fun toDictName(index: Int) = prefix + strs[index].first().toUpperCase() + strs[index].substring(1)
    val size = strs.size
}


object ModRecipes {

    val electrolyzerRecipes = ArrayList<ElectrolyzerRecipe>()
    val dissolverRecipes = ArrayList<DissolverRecipe>()
    val combinerRecipes = ArrayList<CombinerRecipe>()
    val evaporatorRecipes = ArrayList<EvaporatorRecipe>()
    val alloyRecipes = ArrayList<AlloyRecipe>()

    val metals: List<String> = listOf(//not all technically metals, I know
            "aluminum",
            "arsenic",
            "beryllium",
            "bismuth",
            "boron",
            "cadmium",
            "calcium",
            "cerium",
            "chromium",
            "cobalt",
            "copper",
            "dysprosium",
            "erbium",
            "gadolinium",
            "gold",
            "holmium",
            "iridium",
            "iron",
            "lanthanum",
            "lead",
            "lithium",
            "lutetium",
            "magnesium",
            "manganese",
            "neodymium",
            "nickel",
            "niobium",
            "osmium",
            "phosphorus",
            "platinum",
            "potassium",
            "praseodymium",
            "samarium",
            "scandium",
            "silicon",
            "silver",
            "sodium",
            "sulfur",
            "tantalum",
            "terbium",
            "thorium",
            "thulium",
            "tin",
            "titanium",
            "tungsten",
            "uranium",
            "ytterbium",
            "yttrium",
            "zinc")

    val metalOreData: List<DissolverOreData> = listOf(
            DissolverOreData("ingot", 16, metals),
            DissolverOreData("ore", 32, metals),
            DissolverOreData("dust", 16, metals),
            DissolverOreData("block", 144, metals),
            DissolverOreData("nugget", 1, metals),
            DissolverOreData("plate", 16, metals))


    fun init() {
        initElectrolyzerRecipes()
        initEvaporatorRecipes()
        initFuelHandler()
        initAlloyRecipes()
        initDissolverRecipes() //before combiner, so combiner can use reversible recipes
        initCombinerRecipes()
    }

    fun initAlloyRecipes(){
        alloyRecipes.apply{
           // add(AlloyRecipe())
        }
    }


    fun initDissolverRecipes() {

        CompoundRegistry.compounds
                .filter { it.autoDissolverRecipe }
                .forEach { compound ->
                    dissolverRecipes.add(dissolverRecipe {
                        stack = compound.toItemStack(1)
                        inputQuantity = 1
                        output {
                            addGroup {
                                compound.components.forEach { component ->
                                    addStack { component.compound.toItemStack(component.quantity) }
                                }
                            }
                        }
                    })
                }

        for (meta in 0..BlockTallGrass.EnumType.values().size) {
            dissolverRecipes.add(dissolverRecipe {
                stack = Blocks.TALLGRASS.toStack(meta = BlockTallGrass.EnumType.byMetadata(meta).ordinal)
                output {
                    relativeProbability = false
                    addGroup { addStack { "cellulose".toCompoundStack() }; probability = 50 }
                }
            })
        }

        dissolverRecipes.add(dissolverRecipe {
            stack = Blocks.NETHERRACK.toStack()
            output {
                addGroup { addStack { ItemStack.EMPTY }; probability = 15 }
                addGroup { addStack { "zinc_oxide".toCompoundStack() }; probability = 2 }
                addGroup { addStack { "gold".toElementStack() }; probability = 1 }
                addGroup { addStack { "phosphorus".toElementStack() }; probability = 1 }
                addGroup { addStack { "sulfur".toElementStack() }; probability = 1 }
                addGroup { addStack { "silicon".toElementStack() }; probability = 4 }
            }
        })

        dissolverRecipes.add(dissolverRecipe {
            stack = Items.NETHERBRICK.toStack()
            output {
                addGroup { addStack { ItemStack.EMPTY }; probability = 10 }
                addGroup { addStack { "zinc_oxide".toCompoundStack() }; probability = 2 }
                addGroup { addStack { "gold".toElementStack() }; probability = 1 }
                addGroup { addStack { "phosphorus".toElementStack() }; probability = 1 }
                addGroup { addStack { "sulfur".toElementStack() }; probability = 1 }
                addGroup { addStack { "silicon".toElementStack() }; probability = 4 }
            }
        })

        dissolverRecipes.add(dissolverRecipe {
            stack = Items.IRON_HORSE_ARMOR.toStack()
            output {
                addStack { "iron".toElementStack(64) }
            }
        })

        dissolverRecipes.add(dissolverRecipe {
            stack = Items.GOLDEN_HORSE_ARMOR.toStack()
            output {
                addStack { "gold".toElementStack(64) }
            }
        })

        (0 until 16).forEach { index ->
            dissolverRecipes.add(dissolverRecipe {
                stack = Blocks.WOOL.toStack(meta = index)
                output {
                    addStack { "protein".toCompoundStack(2) }
                }
            })
        }

        dissolverRecipes.add(dissolverRecipe {
            stack = Items.EMERALD.toStack()
            output {
                reversible = true
                addGroup {
                    addStack { "beryl".toCompoundStack(16) }
                    addStack { "chromium".toElementStack(8) }
                    addStack { "vanadium".toElementStack(4) }
                }
            }
        })

        dissolverRecipes.add(dissolverRecipe {
            stack = Blocks.END_STONE.toStack()
            output {
                addGroup { addStack { "mercury".toElementStack() }; probability = 60 }
                addGroup { addStack { "neodymium".toElementStack() }; probability = 4 }
                addGroup { addStack { "silicon_dioxide".toCompoundStack(2) }; probability = 300 }
                addGroup { addStack { "lithium".toElementStack() }; probability = 50 }
            }
        })

        dissolverRecipes.add(dissolverRecipe
        {
            stack = Blocks.EMERALD_BLOCK.toStack()
            output {
                addGroup {
                    addStack { "beryl".toCompoundStack(16 * 9) }
                    addStack { "chromium".toElementStack(8 * 9) }
                    addStack { "vanadium".toElementStack(4 * 9) }
                }
            }
        })

        dissolverRecipes.add(dissolverRecipe
        {
            dictName = "blockGlass"
            output {
                addStack { "silicon_dioxide".toCompoundStack(4) }
            }
        })

        dissolverRecipes.add(dissolverRecipe
        {
            dictName = "treeSapling"
            output {
                relativeProbability = false
                addGroup { addStack { "cellulose".toCompoundStack(1) }; probability = 50 }
            }
        })

        dissolverRecipes.add(dissolverRecipe
        {
            stack = Blocks.PUMPKIN.toStack()
            output {
                relativeProbability = false
                addGroup {
                    probability = 50
                    addStack { "cucurbitacin".toCompoundStack() }
                }
            }
        })

        dissolverRecipes.add(dissolverRecipe
        {
            stack = Items.QUARTZ.toStack()
            reversible = true
            output {
                addGroup {
                    addStack { "barium".toElementStack(8) }
                    addStack { "silicon_dioxide".toCompoundStack(16) }
                }
            }
        })

        dissolverRecipes.add(dissolverRecipe
        {
            stack = Blocks.BROWN_MUSHROOM.toStack()
            reversible = true
            output {
                addGroup {
                    addStack { "psilocybin".toCompoundStack() }
                    addStack { "cellulose".toCompoundStack() }
                }
            }
        })

        dissolverRecipes.add(dissolverRecipe
        {
            stack = Blocks.RED_MUSHROOM.toStack()
            reversible = true
            output {
                addGroup {
                    addStack { "cellulose".toCompoundStack() }
                    addStack { "psilocybin".toCompoundStack() }
                }
            }
        })

        dissolverRecipes.add(dissolverRecipe
        {
            stack = Items.DYE.toStack(quantity = 4, meta = 4) //lapis
            output {
                reversible = true
                addGroup {
                    addStack { "sodium".toElementStack(6) }
                    addStack { "calcium".toElementStack(2) }
                    addStack { "aluminum".toElementStack(6) }
                    addStack { "silicon".toElementStack(6) }
                    addStack { "oxygen".toElementStack(24) }
                    addStack { "sulfur".toElementStack(2) }

                }
            }
        })

        dissolverRecipes.add(dissolverRecipe
        {
            stack = Blocks.LAPIS_BLOCK.toStack()
            output {
                addGroup {
                    addStack { "sodium".toElementStack(6 * 9) }
                    addStack { "calcium".toElementStack(2 * 9) }
                    addStack { "aluminum".toElementStack(6 * 9) }
                    addStack { "silicon".toElementStack(6 * 9) }
                    addStack { "oxygen".toElementStack(24 * 9) }
                    addStack { "sulfur".toElementStack(2 * 9) }
                }
            }
        })

        dissolverRecipes.add(dissolverRecipe
        {
            stack = Items.STRING.toStack()
            output {
                relativeProbability = false
                addGroup {
                    probability = 50
                    addStack { "protein".toCompoundStack() }
                }
            }
        })

        dissolverRecipes.add(dissolverRecipe
        {
            stack = ModItems.condensedMilk.toStack()

            output {
                relativeProbability = false
                addGroup { addStack { "calcium".toElementStack(4) }; probability = 40 }
                addGroup { addStack { "protein".toCompoundStack() }; probability = 20 }
                addGroup { addStack { "sucrose".toCompoundStack() }; probability = 20 }
            }
        })

        dissolverRecipes.add(dissolverRecipe
        {
            stack = Items.WHEAT.toStack()
            output {
                relativeProbability = false
                addGroup { addStack { "starch".toCompoundStack() }; probability = 5 }
                addGroup { addStack { "cellulose".toCompoundStack() }; probability = 25 }
            }
        })

        dissolverRecipes.add(dissolverRecipe
        {
            stack = Blocks.HAY_BLOCK.toStack()
            output {
                rolls = 9
                relativeProbability = false
                addGroup { addStack { "starch".toCompoundStack() }; probability = 5 }
                addGroup { addStack { "cellulose".toCompoundStack() }; probability = 25 }
            }
        })

        dissolverRecipes.add(dissolverRecipe
        {
            stack = Items.POTATO.toStack()
            output {
                relativeProbability = false
                addGroup { addStack { "starch".toCompoundStack() }; probability = 10 }
                addGroup { addStack { "potassium".toElementStack(5) }; probability = 25 }
            }
        })

        dissolverRecipes.add(dissolverRecipe
        {
            stack = Items.REDSTONE.toStack()
            output {
                reversible = true
                addGroup {
                    addStack { "iron_oxide".toCompoundStack() }
                    addStack { "strontium_carbonate".toCompoundStack() }
                }
            }
        })

        dissolverRecipes.add(dissolverRecipe {
            dictName = "dyeRed"
            output {
                addStack { "iron_oxide".toCompoundStack(quantity = 2) }
            }
        })

        dissolverRecipes.add(dissolverRecipe {
            dictName = "dyeYellow"
            output {
                addStack { "lead_iodide".toCompoundStack(quantity = 2) }
            }
        })

        dissolverRecipes.add(dissolverRecipe
        {
            stack = Blocks.REDSTONE_BLOCK.toStack()
            output {
                addGroup {
                    addStack { "iron_oxide".toCompoundStack(9) }
                    addStack { "strontium_carbonate".toCompoundStack(9) }
                }
            }
        })
        dissolverRecipes.add(dissolverRecipe
        {
            stack = "protein".toCompoundStack()
            output {
                rolls = 14
                addGroup { addStack { "oxygen".toElementStack() }; probability = 10 }
                addGroup { addStack { "carbon".toElementStack() }; probability = 30 }
                addGroup { addStack { "nitrogen".toElementStack() }; probability = 5 }
                addGroup { addStack { "sulfur".toElementStack() }; probability = 5 }
                addGroup { addStack { "hydrogen".toElementStack() }; probability = 20 }
            }
        })


        dissolverRecipes.add(dissolverRecipe
        {
            stack = Blocks.CLAY.toStack()
            output {
                addStack { "kaolinite".toCompoundStack(4) }
            }
        })

        dissolverRecipes.add(dissolverRecipe
        {
            stack = Items.CLAY_BALL.toStack()
            reversible = true
            output {
                addStack { "kaolinite".toCompoundStack() }
            }
        })

        dissolverRecipes.add(dissolverRecipe
        {
            stack = Items.SUGAR.toStack()
            reversible = true
            output {
                addStack { "sucrose".toCompoundStack() }
            }
        })

        dissolverRecipes.add(dissolverRecipe
        {
            stack = Items.BONE.toStack()
            reversible = true
            output {
                addStack { "hydroxylapatite".toCompoundStack(2) }
            }
        })

        dissolverRecipes.add(dissolverRecipe
        {
            stack = Items.DYE.toStack(meta = 15) //bonemeal
            output {
                relativeProbability = false
                addGroup { addStack { "hydroxylapatite".toCompoundStack(1) }; probability = 50 }
            }
        })

        dissolverRecipes.add(dissolverRecipe
        {
            stack = Items.EGG.toStack()
            reversible = true
            output {
                addGroup {
                    addStack { "calcium_carbonate".toCompoundStack(8) }
                    addStack { "protein".toCompoundStack(2) }
                }
            }
        })

        dissolverRecipes.add(dissolverRecipe
        {
            stack = ModItems.mineralSalt.toStack()
            output {
                addGroup { addStack { "sodium_chloride".toCompoundStack() }; probability = 80 }
                addGroup { addStack { "lithium".toElementStack() }; probability = 2 }
                addGroup { addStack { "potassium_chloride".toCompoundStack() }; probability = 5 }
                addGroup { addStack { "iron".toElementStack() }; probability = 2 }
                addGroup { addStack { "copper".toElementStack() }; probability = 2 }
                addGroup { addStack { "zinc".toElementStack() } }
            }
        })

        dissolverRecipes.add(dissolverRecipe
        {
            stack = Items.COAL.toStack()
            reversible = true
            output {
                addStack { "carbon".toElementStack(quantity = 8) }
            }
        })

        dissolverRecipes.add(dissolverRecipe
        {
            dictName = "slabWood"
            inputQuantity = 8
            output {
                addStack { "cellulose".toCompoundStack() }
            }
        })


        if (oreExists("itemSilicon")) {
            dissolverRecipes.add(dissolverRecipe {
                dictName = "itemSilicon"
                reversible = true
                output {
                    addStack { "silicon".toElementStack(16) }
                }
            })
        }

        dissolverRecipes.add(dissolverRecipe
        {
            stack = Items.ENDER_PEARL.toStack()
            reversible = true
            output {
                addGroup {
                    addStack { "silicon".toElementStack(16) }
                    addStack { "mercury".toElementStack(16) }
                    addStack { "neodymium".toElementStack(16) }
                }
            }
        })

        dissolverRecipes.add(dissolverRecipe
        {
            stack = Items.DIAMOND.toStack()
            output {
                addStack { "carbon".toElementStack(quantity = 64 * 8) }
            }
        })

        dissolverRecipes.add(dissolverRecipe
        {
            dictName = "plankWood"
            inputQuantity = 4
            output {
                addStack { "cellulose".toCompoundStack() }
            }
        })

        dissolverRecipes.add(dissolverRecipe
        {
            dictName = "cobblestone"
            output {
                addGroup { addStack { ItemStack.EMPTY }; probability = 350 }
                addGroup { addStack { "aluminum".toElementStack(1) } }
                addGroup { addStack { "iron".toElementStack(1) }; probability = 2 }
                addGroup { addStack { "gold".toElementStack(1) } }
                addGroup { addStack { "silicon_dioxide".toCompoundStack(1) }; probability = 5 }
            }
        })

        dissolverRecipes.add(dissolverRecipe
        {
            dictName = "stoneGranite"
            output {
                addGroup { addStack { ItemStack.EMPTY }; probability = 150 }
                addGroup { addStack { "aluminum_oxide".toCompoundStack(1) }; probability = 5 }
                addGroup { addStack { "iron".toElementStack(1) }; probability = 2 }
                addGroup { addStack { "potassium_chloride".toCompoundStack(1) }; probability = 2 }
                addGroup { addStack { "silicon_dioxide".toCompoundStack(1) }; probability = 10 }
            }
        })

        dissolverRecipes.add(dissolverRecipe
        {
            dictName = "stoneDiorite"
            output {
                addGroup { addStack { ItemStack.EMPTY }; probability = 150 }
                addGroup { addStack { "aluminum_oxide".toCompoundStack(1) }; probability = 5 }
                addGroup { addStack { "iron".toElementStack(1) }; probability = 2 }
                addGroup { addStack { "potassium_chloride".toCompoundStack(1) }; probability = 2 }
                addGroup { addStack { "silicon_dioxide".toCompoundStack(1) }; probability = 10 }
            }
        })

        dissolverRecipes.add(dissolverRecipe
        {
            stack = Blocks.MAGMA.toStack()
            output {
                rolls = 2
                addGroup { addStack { ItemStack.EMPTY }; probability = 100 }
                addGroup { addStack { "aluminum_oxide".toCompoundStack(1) }; probability = 5 }
                addGroup { addStack { "magnesium_oxide".toCompoundStack(1) }; probability = 20 }
                addGroup { addStack { "potassium_chloride".toCompoundStack(1) }; probability = 2 }
                addGroup { addStack { "silicon_dioxide".toCompoundStack(2) }; probability = 10 }
                addGroup { addStack { "sulfur".toElementStack() }; probability = 5 }
            }
        })

        dissolverRecipes.add(dissolverRecipe
        {
            dictName = "treeLeaves"
            output {
                relativeProbability = false
                addGroup { addStack { "cellulose".toCompoundStack() }; probability = 2 }
            }
        })

        dissolverRecipes.add(dissolverRecipe
        {
            dictName = "stoneAndesite"
            output {
                addGroup { addStack { ItemStack.EMPTY }; probability = 150 }
                addGroup { addStack { "aluminum_oxide".toCompoundStack(1) }; probability = 5 }
                addGroup { addStack { "iron".toElementStack(1) }; probability = 2 }
                addGroup { addStack { "potassium_chloride".toCompoundStack(1) }; probability = 2 }
                addGroup { addStack { "silicon_dioxide".toCompoundStack(1) }; probability = 10 }
            }
        })

        dissolverRecipes.add(dissolverRecipe
        {
            dictName = "stone"
            output {
                addGroup { addStack { ItemStack.EMPTY }; probability = 150 }
                addGroup { addStack { "aluminum".toElementStack(1) } }
                addGroup { addStack { "iron".toElementStack(1) }; probability = 2 }
                addGroup { addStack { "gold".toElementStack(1) } }
                addGroup { addStack { "silicon_dioxide".toCompoundStack(1) }; probability = 5 }
            }
        })

        dissolverRecipes.add(dissolverRecipe
        {
            dictName = "sand"
            output {
                relativeProbability = false
                addGroup { addStack { "silicon_dioxide".toCompoundStack(quantity = 4) }; probability = 100 }
                addGroup { addStack { "gold".toElementStack() } }
            }
        })

        dissolverRecipes.add(dissolverRecipe
        {
            stack = Items.GUNPOWDER.toStack()
            reversible = true
            output {
                addGroup {
                    addStack { "potassium_nitrate".toCompoundStack(2) }
                    addStack { "sulfur".toElementStack(8) }
                    addStack { "carbon".toElementStack(8) }
                }
            }
        })

        dissolverRecipes.add(dissolverRecipe
        {
            dictName = "logWood"
            inputQuantity = 1
            output {
                addStack { "cellulose".toCompoundStack() }
            }
        })

        metalOreData.forEach { list ->
            (0 until list.size).forEach { index ->
                val elementName = list.strs[index]
                val oreName = list.toDictName(index)
                if (OreDictionary.doesOreNameExist(oreName)) {
                    dissolverRecipes.add(dissolverRecipe {
                        dictName = oreName
                        inputQuantity = 1
                        output {
                            addStack {
                                ModItems.elements.toStack(quantity = list.quantity, meta = ElementRegistry.getMeta(elementName))
                            }
                        }
                    })
                }
            }
        }

        dissolverRecipes.add(dissolverRecipe
        {
            stack = ItemStack(Items.GLOWSTONE_DUST)
            reversible = true
            output {
                addStack { "phosphorus".toElementStack(quantity = 4) }
            }
        })

        dissolverRecipes.add(dissolverRecipe
        {
            stack = ItemStack(Blocks.IRON_BARS)
            output {
                addStack { "iron".toElementStack(quantity = 6) }
            }
        })

        if (oreExists("dropHoney")) {
            dissolverRecipes.add(dissolverRecipe {
                dictName = "dropHoney"
                output {
                    addStack { "sucrose".toCompoundStack(quantity = 4) }
                }
            })
        }

        if (oreExists("shardPrismarine")) {
            dissolverRecipes.add(dissolverRecipe {
                dictName = "shardPrismarine"
                reversible = true
                output {
                    addGroup {
                        addStack { "beryl".toCompoundStack(quantity = 2) }
                        addStack { "cobalt_aluminate".toCompoundStack(quantity = 4) }
                    }
                }
            })
        }

        if (oreExists("ingotElectrum")) {
            dissolverRecipes.add(dissolverRecipe {
                dictName = "ingotElectrum"
                reversible = true
                output {
                    addGroup {
                        addStack { "gold".toElementStack(quantity = 8) }
                        addStack { "silver".toElementStack(quantity = 8) }
                    }
                }
            })
        }

        if (oreExists("ingotBronze")) {
            dissolverRecipes.add(dissolverRecipe {
                dictName = "ingotBronze"
                reversible = true
                output {
                    addGroup {
                        addStack { "copper".toElementStack(quantity = 12) }
                        addStack { "tin".toElementStack(quantity = 4) }
                    }
                }
            })
        }

        dissolverRecipes.add(dissolverRecipe {
            stack = Blocks.MELON_BLOCK.toStack()

            output {
                addGroup {
                    relativeProbability = false
                    probability = 50
                    addStack { "cucurbitacin".toCompoundStack(); }
                }
                addGroup {
                    addStack { "water".toCompoundStack(quantity = 4) }
                    addStack { "sucrose".toCompoundStack(quantity = 2) }
                }
            }
        })

        if (oreExists("itemSalt")) {
            dissolverRecipes.add(dissolverRecipe {
                dictName = "itemSalt"
                reversible = true
                output {
                    addStack { "sodium_chloride".toCompoundStack(quantity = 16) }
                }
            })
        }
    }


    fun initElectrolyzerRecipes() {
        electrolyzerRecipes.add(ElectrolyzerRecipe(FluidStack(FluidRegistry.WATER, 62),
                listOf("calcium_carbonate".toCompoundStack()), 20,
                "hydrogen".toElementStack(2), "oxygen".toElementStack(1)))

        electrolyzerRecipes.add(ElectrolyzerRecipe(FluidStack(FluidRegistry.WATER, 62),
                listOf("sodium_chloride".toCompoundStack()), 20,
                outputOne = "hydrogen".toElementStack(2),
                outputTwo = "oxygen".toElementStack(1),
                outputThree = "chlorine".toElementStack(2), output3Probability = 10))
    }


    fun initEvaporatorRecipes() {
        evaporatorRecipes.add(EvaporatorRecipe(FluidRegistry.WATER, 100, ModItems.mineralSalt.toStack()))
        FluidRegistry.getFluid("milk")?.let {
            evaporatorRecipes.add(EvaporatorRecipe(FluidRegistry.getFluid("milk"), 500, ModItems.condensedMilk.toStack()))
        }
    }


    fun initFuelHandler() {
        val fuelHandler = IFuelHandler { fuel ->
            if (fuel.item == ModItems.elements) {
                when (fuel.itemDamage) {
                    ElementRegistry["hydrogen"]!!.meta -> return@IFuelHandler 10
                    ElementRegistry["carbon"]!!.meta -> return@IFuelHandler 200
                }
            }
            return@IFuelHandler 0
        }
        GameRegistry.registerFuelHandler(fuelHandler)
    }


    fun initCombinerRecipes() {

        combinerRecipes.add(CombinerRecipe(Items.COAL.toStack(meta = 1),
                listOf(ItemStack.EMPTY, "carbon".toElementStack(4))))


        metals.forEach { entry ->
            val dustOutput: ItemStack? = firstOre(entry.toDict("dust"))
            if (dustOutput != null && !dustOutput.isEmpty) {
                combinerRecipes.add(CombinerRecipe(dustOutput,
                        listOf(entry.toElementStack(4), entry.toElementStack(4), entry.toElementStack(4),
                                entry.toElementStack(4))))
            }

            val ingotOutput: ItemStack? = firstOre(entry.toDict("ingot"))
            if (ingotOutput != null && !ingotOutput.isEmpty) {
                combinerRecipes.add(CombinerRecipe(ingotOutput,
                        listOf(entry.toElementStack(16))))
            }
        }

        CompoundRegistry.compounds
                .filter { it.autoCombinerRecipe }
                .forEach { compound ->
                    combinerRecipes.add(CombinerRecipe(compound.toItemStack(1), compound.toItemStackList()))
                }

        dissolverRecipes.filter { it.reversible }.forEach { recipe ->
            if (recipe.inputs.size > 0) combinerRecipes.add(CombinerRecipe(recipe.inputs[0], recipe.outputs.toStackList()))
        }


        val carbon = "carbon".toElementStack(quantity = 64)
        combinerRecipes.add(CombinerRecipe(Items.DIAMOND.toStack(),
                listOf(carbon, carbon, carbon,
                        carbon, null, carbon,
                        carbon, carbon, carbon)))

        combinerRecipes.add(CombinerRecipe(Blocks.SAND.toStack(),
                listOf("silicon_dioxide".toCompoundStack(), "silicon_dioxide".toCompoundStack(), null,
                        "silicon_dioxide".toCompoundStack(), "silicon_dioxide".toCompoundStack())))

        combinerRecipes.add(CombinerRecipe(Blocks.COBBLESTONE.toStack(quantity = 2),
                listOf("silicon_dioxide".toCompoundStack())))

        combinerRecipes.add(CombinerRecipe(Blocks.STONE.toStack(),
                listOf(ItemStack.EMPTY, "silicon_dioxide".toCompoundStack())))

        combinerRecipes.add(CombinerRecipe(Blocks.OBSIDIAN.toStack(),
                listOf("magnesium_oxide".toCompoundStack(8), "potassium_chloride".toCompoundStack(8), "aluminum_oxide".toCompoundStack(8),
                        "silicon_dioxide".toCompoundStack(8), "silicon_dioxide".toCompoundStack(8), "silicon_dioxide".toCompoundStack(8)
                )))

        combinerRecipes.add(CombinerRecipe(Blocks.CLAY.toStack(),
                listOf(ItemStack.EMPTY, "kaolinite".toCompoundStack(4))))

        combinerRecipes.add(CombinerRecipe(Blocks.DIRT.toStack(4),
                listOf("water".toCompoundStack(1), "cellulose".toCompoundStack(1), "kaolinite".toCompoundStack(1))))

        combinerRecipes.add(CombinerRecipe(Blocks.GRAVEL.toStack(),
                listOf(null, null, "silicon_dioxide".toCompoundStack(2))))

        combinerRecipes.add(CombinerRecipe(Items.WATER_BUCKET.toStack(),
                listOf(null, null, null,
                        null, "water".toCompoundStack(16), null,
                        null, Items.BUCKET, null)))


        combinerRecipes.add(CombinerRecipe(Items.POTIONITEM.toStack()
                .apply { this.setTagInfo("Potion", net.minecraft.nbt.NBTTagString("water")) },
                listOf(null, null, null,
                        null, "water".toCompoundStack(16), null,
                        null, Items.GLASS_BOTTLE, null)))

        combinerRecipes.add(CombinerRecipe(Blocks.REDSTONE_BLOCK.toStack(),
                listOf(null, null, null,
                        "iron_oxide".toCompoundStack(36), "strontium_carbonate".toCompoundStack(36))))

        combinerRecipes.add(CombinerRecipe(Items.STRING.toStack(4),
                listOf(null, "protein".toCompoundStack(), null,
                        null, "protein".toCompoundStack())))


        combinerRecipes.add(CombinerRecipe(Items.REEDS.toStack(),
                listOf(null, null, null,
                        "cellulose".toCompoundStack(), "sucrose".toCompoundStack())))

        Item.REGISTRY.getObject(ResourceLocation("forestry", "iodine_capsule"))?.let {
            combinerRecipes.add(CombinerRecipe(it.toStack(),
                    listOf(null, null, null,
                            "iodine".toElementStack(8), "iodine".toElementStack(8))))
        }

        (0..5).forEach { i ->
            val input = (0 until i).mapTo(ArrayList<ItemStack>(), { ItemStack.EMPTY })
            input.add("cellulose".toCompoundStack())
            input.add("cellulose".toCompoundStack())
            combinerRecipes.add(CombinerRecipe(Blocks.SAPLING.toStack(quantity = 4, meta = i), input))
        }

        (0 until 5).forEach { i ->
            val input = (0 until i).mapTo(ArrayList<ItemStack>(), { ItemStack.EMPTY })
            input.add("cellulose".toCompoundStack())

            //y u gotta do dis mojang
            if (i < 3) combinerRecipes.add(CombinerRecipe(ItemStack(Blocks.LOG, 1, i), input))
            else combinerRecipes.add(CombinerRecipe(ItemStack(Blocks.LOG2, 1, i - 3), input))
        }
    }
}