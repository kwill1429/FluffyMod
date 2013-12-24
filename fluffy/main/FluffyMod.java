package fluffy.main;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.NetworkMod;
import fluffy.din.DinLoggerStuff;
import fluffy.main.api.*;
import fluffy.main.proxy.CommonProxy;
import fluffy.messenger.Messenger;

@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.MOD_VERSION)
@NetworkMod(clientSideRequired = Reference.MOD_REQUIRED_CLIENT, serverSideRequired = Reference.MOD_REQUIRED_SERVER)
public class FluffyMod {
	
	@Instance(Reference.MOD_ID)
	public static FluffyMod instance;
	
	@SidedProxy(clientSide = Reference.CLIENT_PROXY_CLASS, serverSide = Reference.SERVER_PROXY_CLASS)
	public static CommonProxy proxy;
	
	ConfigHelper configs;
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		
        LoggingHelper.getInstance().info("Fluffy mod is Loading");
        DinLoggerStuff.preSwag();
        DinLoggerStuff.modMsg();
        
        configs = new ConfigHelper(event);

        Messenger.initPre(configs);
        
	}
	
	@EventHandler
	public void init(FMLInitializationEvent event) {
		//Init your classes here! 	
		//Please only one class per a contributor  ( more is allowed tho )
		Messenger.initMain();
	}

    @EventHandler
    public void postInit (FMLPostInitializationEvent event)
    {
  
    	configs.saveConfigFile();
    	
    }
    
	@EventHandler
	public void onServerStarting(FMLServerStartingEvent event) {
		Messenger.initStarting(event);
	}
}

	
	

