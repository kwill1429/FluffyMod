package fluffy.main;

import java.util.logging.Logger;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import fluffy.main.proxy.CommonProxy;

@Mod(modid = "fluffymod", name = "Fluffy mod", version = "Infinite development")
@NetworkMod(clientSideRequired = true)


public class FluffyMod {
	
	
	@Instance("fluffymod")
	public static FluffyMod instance;
	@SidedProxy(clientSide = "fluffy.main.proxy.ClientProxy", serverSide = "fluffy.main.proxy.CommonProxy")
	public static CommonProxy proxy;
	public static final Logger logger = Logger.getLogger("Fluffy Mod");	
	
	

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		
		
        logger.setParent(FMLCommonHandler.instance().getFMLLogger());
        FluffyMod.logger.info("Fluffy mod is Loading");
  
  
	}
	
	
	@EventHandler
	public void init(FMLInitializationEvent event) {
	//Init your classes here! 	
	//Please only one class per a contributor  ( more is allowed tho )
		
		
		
	}
	
	
	
    @EventHandler
    public void postInit (FMLPostInitializationEvent event)
    {
  
    }
    
  
    
    
}

	
	

