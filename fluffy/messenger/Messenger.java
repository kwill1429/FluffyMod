package fluffy.messenger;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import net.minecraft.client.Minecraft;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatMessageComponent;
import net.minecraftforge.common.Configuration;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.IPlayerTracker;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import fluffy.main.api.ConfigHelper;
import fluffy.main.api.LoggingHelper;

/**
 * Main Class used for all Initializations
 * @author kwill
 *
 */
public class Messenger
{
	private static boolean enabled;
	public static ConfigHelper config;
	public static void initStarting(FMLServerStartingEvent event)
	{
		if(getEnabled(config))
			event.registerServerCommand(new MessengerCommand());
	}
	
	public static void initPre(ConfigHelper par1configs)
	{
		config=par1configs;
	}
	
	public static void initMain()
	{
		if(Messenger.getEnabled(Messenger.config))
		{
			LoggingHelper.getInstance().info("Messenger Loaded");
			GameRegistry.registerPlayerTracker(new PlayerTracker());
			MessengerCommand.loadMessages(Message.readMessagesFromFile());
		}
		else
		{
			LoggingHelper.getInstance().info("Messenger Disabled. Please enable in the config.");
		}
	}
	public static boolean getEnabled(ConfigHelper configs)
	{
		return configs.getFeature("Messenger", "A tool used to send messages to players on/off line. Type /help mail for more.");
	}
}

class MessengerCommand implements ICommand
{
	private List<String> aliases;
	private static List<Message> messages = new ArrayList<>();
	public void Messenger()
	{
		this.aliases=new ArrayList<String>();
		this.aliases.add("mail");
		this.aliases.add("m");
	}
	
	@Override
	public int compareTo(Object o) {
		return 0;
	}

	@Override
	public String getCommandName() {
		return "mail";
	}

	@Override
	public String getCommandUsage(ICommandSender icommandsender) {
		icommandsender.sendChatToPlayer(new ChatMessageComponent().addText("/mail <username> <message> //Saves message to be shown to <username> next time they log in."));
		icommandsender.sendChatToPlayer(new ChatMessageComponent().addText("/mail undo //Prevents the previous message from being shown if they haven't already seen it. (Will keep working the more times you send it)"));
		icommandsender.sendChatToPlayer(new ChatMessageComponent().addText("/mail read // Displays all messages you have sent (That have not been read by the receiver)"));
		return "";
	}

	@Override
	public List<String> getCommandAliases() {
		return this.aliases;
	}

	@Override
	public void processCommand(ICommandSender icommandsender, String[] astring) {
		List<Message> toBeRemoved = new ArrayList<>();
		if (astring.length < 1)
		{
			getCommandUsage(icommandsender);
			return;
		}
		
		if (astring.length == 1)
		{
			if (astring[0].equals("undo"))
			{
				if (!messages.equals(null)&&messages.size()>0)
				{
					ListIterator<Message> li = messages.listIterator(messages.size());
					while(li.hasPrevious())
					{
						Message temp = li.previous();
						if (temp.getSender().equals(icommandsender.getCommandSenderName()))
						{
							toBeRemoved.add(temp);
							icommandsender.sendChatToPlayer(ChatMessageComponent.createFromText("[Removed message] Receiver: "+temp.getReceiver()+" Message: " + temp.getMessage()));
							break;
						}
					}
					for (Message oldMessage : toBeRemoved) {
						MessengerCommand.removeMessage(oldMessage);
					}
				} else
				{
					icommandsender.sendChatToPlayer(ChatMessageComponent.createFromText("[Mail] You have not sent anything or it has already been read."));
				}
				
			}
			else if(astring[0].equals("read"))
			{
				for (Message mess : messages) {
					if(mess.getSender()==icommandsender.getCommandSenderName())
						icommandsender.sendChatToPlayer(ChatMessageComponent.createFromText("[Mail] "+mess.getSender()+"->"+mess.getReceiver()+": "+mess.getMessage()));
				}
			}
		}
		else if (astring.length >= 2)
		{
			String message = "";
			for (int i = 1; i < astring.length; i++) {
				message+= i<(astring.length-1) ? astring[i]+" " : astring[i];
			}
			messages.add(new Message(icommandsender.getCommandSenderName(), astring[0], message));
			if(!PlayerTracker.getUsersLoggedIn().contains(astring[0]))
				Message.writeMessagesToFile(messages);
			else
			{
				icommandsender.getEntityWorld().getPlayerEntityByName(astring[0]).sendChatToPlayer(ChatMessageComponent.createFromText("[Mail] "+icommandsender.getCommandSenderName()+" says: "+message));;
				MessengerCommand.removeMessage(messages.get(messages.size()-1));
			}
			icommandsender.sendChatToPlayer(ChatMessageComponent.createFromText("[Mail] Message Sent"));
			
		}
		
		
	}

	@Override
	public boolean canCommandSenderUseCommand(ICommandSender icommandsender) {
		return true;
	}

	@Override
	public List<?> addTabCompletionOptions(ICommandSender icommandsender, String[] astring) {
		return null;
	}

	@Override
	public boolean isUsernameIndex(String[] astring, int i) {
		for (int j = 0; j < astring.length; j++)
			if (j==0)
				return true;
		return false;
	}
	
	public static void loadMessages(List<Message> messagesInput)
	{
		if (messagesInput!=null)
			messages.addAll(messagesInput);
	}
	
	public static List<Message> getMessages()
	{
		List<Message> tempMessage = new ArrayList<>();
		tempMessage.addAll(messages);
		return tempMessage;
		
	}
	public static void removeMessage(Message par1message)
	{
		messages.remove(par1message);
		Message.writeMessagesToFile(messages);
	}
}

class PlayerTracker implements IPlayerTracker
{
	private String username;
	private List<Message> playerMessages = new ArrayList<>();
	private static List<String> usersLoggedIn = new ArrayList<String>();
	@Override
	public void onPlayerLogin(EntityPlayer player)
	{
		refreshUsersLoggedIn();
		this.username=player.username;
		playerMessages=MessengerCommand.getMessages();
		ListIterator<Message> li = playerMessages.listIterator();
		while(li.hasNext())
		{
			Message temp = li.next();
			if(temp.getReceiver().equals(this.username))
			{
				player.addChatMessage("[Mail] "+temp.getSender()+": "+temp.getMessage());
				MessengerCommand.removeMessage(temp);
			}
		}
		
	}

	@Override
	public void onPlayerLogout(EntityPlayer player)
	{
			refreshUsersLoggedIn();
	}
	
	@Override
	public void onPlayerChangedDimension(EntityPlayer player) {}
	
	@Override
	public void onPlayerRespawn(EntityPlayer player) {}
	
	public static List<String> getUsersLoggedIn()
	{
		return usersLoggedIn;
	}
	
	public static void refreshUsersLoggedIn()
	{
		usersLoggedIn.clear();
		for (int i = 0; i < MinecraftServer.getServer().getAllUsernames().length; i++) {
			usersLoggedIn.add(MinecraftServer.getServer().getAllUsernames()[i]);
		}
	}
	
}

class Message implements Serializable
{
	
	private static final long serialVersionUID = 6139068408931747492L;
	private String sender;
	private String receiver;
	private String message;
	
	private static File messageDir = setMessageDir();
	private static File messageFile = new File(messageDir,"message.dat");
	public Message(String sender, String reciever, String message)
	{
		this.sender=sender;
		this.receiver=reciever;
		this.message=message;
	}
	public String getSender()
	{
		return this.sender;
	}
	public String getReceiver()
	{
		return this.receiver;
	}
	
	public static File setMessageDir()
	{
		Side s = FMLCommonHandler.instance().getEffectiveSide();
		if(s==Side.SERVER)
			return new File("./config/Messenger");
		else if (s==Side.CLIENT)
			return new File(Minecraft.getMinecraft().mcDataDir,"config/Messenger");
		return null;
		
	}
	
	public String getMessage()
	{
		return this.message;
	}
	public static void writeMessagesToFile(List<Message> par1list)
	{
		ObjectOutputStream oos = null;
		int numMessages = MessengerCommand.getMessages().size();
		if(!messageFile.exists())
		{
			if(!messageDir.exists())
				messageDir.mkdirs();
		}
		try
		{
			oos = new ObjectOutputStream(new FileOutputStream(messageFile));
			oos.writeObject(numMessages);
			for (int i = 0; i < numMessages; i++) {
				oos.writeObject(par1list.get(i));
			}
			oos.flush();
			oos.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
	}
	public static List<Message> readMessagesFromFile()
	{
		ObjectInputStream ois=null;
		Integer readFile = 0;
		List<Message> finalList=new ArrayList<>();
		if(!messageFile.exists())
		{
			try {
				if(!messageDir.exists())
					messageDir.mkdirs();
				messageFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try
		{
			
			ois = new ObjectInputStream(new FileInputStream(messageFile));
			readFile = (Integer)ois.readObject();
			if(readFile!=null)
			{
				for (int i = 0; i < (int)readFile; i++) {
					finalList.add((Message)ois.readObject());
				}
				ois.close();
			}
		} 
		catch (EOFException e)
		{
			return finalList;
		}
		catch (ClassNotFoundException | IOException e) 
		{
			e.printStackTrace();
		}
		
		return finalList;
	}
	
}

class CustomOutputStream extends ObjectOutputStream
{

	public CustomOutputStream(OutputStream out) throws IOException
	{
		super(out);
	}
	@Override
	protected void writeStreamHeader() {}
	
}