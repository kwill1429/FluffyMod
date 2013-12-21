package fluffy.main.api;

import java.io.File;

import net.minecraftforge.common.Configuration;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class ConfigHelper {
    
    Configuration configFile;
    
    public ConfigHelper (FMLPreInitializationEvent event) {
        configFile = new Configuration(event.getSuggestedConfigurationFile());
        configFile.load();
    }
    
    public ConfigHelper (File file) {
    	configFile = new Configuration(file);
    }
    
    public Configuration config () {
        return configFile;
    }
    public void saveConfigFile () {
    	configFile.save();
    }
    
    public int getBlockID(String blockName, int def){
    	return configFile.getBlock(blockName, def).getInt();
    }
    public int getItemID(String blockName, int def){
    	return configFile.getItem(blockName, def).getInt();
    }
}
