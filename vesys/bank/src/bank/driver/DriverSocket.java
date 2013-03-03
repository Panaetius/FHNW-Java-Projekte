package bank.driver;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import bank.Bank;
import bank.BankDriver;
import bank.InactiveException;
import bank.OverdrawException;

public class DriverSocket implements BankDriver {

	private SocketBank b;
	private Inet4Address remote;
	private Socket sock;
	
	
	@Override
	public void connect(String[] args) throws IOException {
		if (args.length < 2) { throw new IOException("provide Host and Port"); }
		else {
			remote = (Inet4Address) Inet4Address.getByName(args[0]);
			sock = new Socket(remote, Integer.parseInt(args[1]));
			System.out.println("Connected to "+remote.getHostName()+":"+args[1]);
			b = new SocketBank(this);
		}
		
	}

	@Override
	public void disconnect() throws IOException {
			sock.close();	
	}

	@Override
	public Bank getBank() {
		return this.b;
	}
	

	static class SocketBank implements bank.Bank {

		private Map<String, SocketAccount> accounts = new HashMap<String, SocketAccount>();
		private DriverSocket driver;
		
		public SocketBank(DriverSocket d) {
			this.driver = d;																											
		}
		
		@Override
		public Set<String> getAccountNumbers() throws IOException {
			Command.send("getAccountNumbers", "", driver.sock);
			String cmd = Command.receive(driver.sock);
			Set<String> activeAccounts = new HashSet<String>();
			System.out.println("Client received: "+cmd);
			
			if (Command.parseCommand(cmd).equals("SUCCESS")) {
				String[] params = Command.parseParams(cmd);
				System.out.println("Accounts: "+params.length);
				for (String param : params) {
					if (!param.isEmpty() || !param.equals("")) activeAccounts.add(param);
				}
			}
			return activeAccounts;
		}

		@Override
		public String createAccount(String owner) throws IOException {
			Command.send("createAccount", owner, driver.sock);
			String cmd = Command.receive(driver.sock);
			System.out.println("Client received: "+cmd);
			String number = Command.parseParams(cmd)[0];
			accounts.put(number, new SocketAccount(owner, number, this.driver));
			return number;
		}

		@Override
		public boolean closeAccount(String number) throws IOException {
			Command.send("closeAccount", number, driver.sock);
			String cmd = Command.receive(driver.sock);
			Boolean r = Command.parseParams(cmd)[0].equals("true");
			System.out.println("Client received: "+cmd);
			SocketAccount acc = accounts.get(number);
			if (acc.isActive() && acc.getBalance() == 0) acc.deactivate();
			return r;
		}

		@Override
		public bank.Account getAccount(String number) {
			return (bank.Account) accounts.get(number);
		}

		@Override
		public void transfer(bank.Account from, bank.Account to, double amount)
				throws IOException, InactiveException, OverdrawException {
			from.withdraw(amount);
			to.deposit(amount);
			
		}
	}
	
	static class SocketAccount implements bank.Account {
			private String number;
			private String owner;
			private double balance;
			private boolean active = true;
			DriverSocket driver;

			SocketAccount(String owner, String number, DriverSocket s) {
				this.owner = owner;
				this.number = number;
				this.driver = s;
				
			}

			@Override
			public double getBalance() throws IOException {
				Command.send("getBalance", this.number, driver.sock);
				return Double.parseDouble(Command.parseParams(Command.receive(driver.sock))[0]);
			}

			@Override
			public String getOwner() throws IOException {
				Command.send("getOwner", this.number, driver.sock);
				return Command.parseParams(Command.receive(driver.sock))[0];
			}

			@Override
			public String getNumber() {
				return number;
			}

			@Override
			public boolean isActive() throws IOException {
				Command.send("isActive", this.number, driver.sock);
				return Command.parseParams(Command.receive(driver.sock))[0].equals("true");
			}

			@Override
			public void deposit(double amount) throws InactiveException, IOException {
				Command.send("deposit", this.number+","+Double.toString(amount), driver.sock);
				String cmd = Command.receive(driver.sock);
				System.out.println("Client received: "+cmd);
				if (!this.isActive()) throw new InactiveException();
				else if (amount < 0) throw new IllegalArgumentException();
				else this.balance += amount;
			}

			@Override
			public void withdraw(double amount) throws InactiveException, OverdrawException, IOException {
				Command.send("deposit", this.number+","+Double.toString(amount), driver.sock);
				String cmd = Command.receive(driver.sock);
				System.out.println("Client received: "+cmd);
				if (!this.isActive()) throw new InactiveException();
				else if (amount < 0) throw new IllegalArgumentException();
				else {
					if (this.getBalance() < amount) throw new OverdrawException();
					else this.balance -= amount;
				}
			}
			
			public void activate() {
				this.active = true;
			}

			public void deactivate() {
				this.active = false;
			}
		}


	

	

}
