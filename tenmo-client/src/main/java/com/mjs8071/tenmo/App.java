package com.mjs8071.tenmo;

import com.mjs8071.tenmo.model.*;
import com.mjs8071.tenmo.services.AuthenticationService;
import com.mjs8071.tenmo.services.ConsoleService;
import com.mjs8071.tenmo.services.TransferService;

public class App {

    private static final String API_BASE_URL = "http://localhost:8080/";

    private final ConsoleService consoleService = new ConsoleService();
    private final AuthenticationService authenticationService = new AuthenticationService(API_BASE_URL);
    private final TransferService transferService = new TransferService();

    private AuthenticatedUser currentUser;

    public static void main(String[] args) {
        App app = new App();
        app.run();
    }

    private void run() {
        consoleService.printGreeting();
        loginMenu();
        if (currentUser != null) {
            mainMenu();
        }
    }
    private void loginMenu() {
        int menuSelection = -1;
        while (menuSelection != 0 && currentUser == null) {
            consoleService.printLoginMenu();
            menuSelection = consoleService.promptForMenuSelection("Please choose an option: ");
            if (menuSelection == 1) {
                handleRegister();
            } else if (menuSelection == 2) {
                handleLogin();
            } else if (menuSelection != 0) {
                System.out.println("Invalid Selection");
                consoleService.pause();
            }
        }
    }

    private void handleRegister() {
        System.out.println("Please register a new user account");
        UserCredentials credentials = consoleService.promptForCredentials();
        if (authenticationService.register(credentials)) {
            System.out.println("Registration successful. You can now login.");
        } else {
            consoleService.printErrorMessage();
        }
    }

    private void handleLogin() {
        UserCredentials credentials = consoleService.promptForCredentials();
        currentUser = authenticationService.login(credentials);
        transferService.setAuthToken(currentUser.getToken());
        if (currentUser == null) {
            consoleService.printErrorMessage();
        }
    }

    private void mainMenu() {
        int menuSelection = -1;
        while (menuSelection != 0) {
            consoleService.printMainMenu();
            menuSelection = consoleService.promptForMenuSelection("Please choose an option: ");
            if (menuSelection == 1) {
                viewCurrentBalance();
            } else if (menuSelection == 2) {
                viewTransferHistory();
            } else if (menuSelection == 3) {
                viewPendingRequests();
            } else if (menuSelection == 4) {
                sendBucks();
            } else if (menuSelection == 5) {
                requestBucks();
            } else if (menuSelection == 0) {
                continue;
            } else {
                System.out.println("Invalid Selection");
            }
            consoleService.pause();
        }
    }

	private void viewCurrentBalance() {
		// TODO Auto-generated method stub

        consoleService.printCurrentBalance(transferService.getBalance());
	}

	private void viewTransferHistory() {
		// TODO Auto-generated method stub
		Transfer[] transfers = transferService.listTransfers();
        if (transfers != null) {
            consoleService.printTransfers(transfers, currentUser.getUser().getUsername());
            Transfer displayTransfer = consoleService.pickTransferFromList(transfers);
            if (displayTransfer != null) {
                consoleService.displayTransfer(displayTransfer);
            }
        } else {
            consoleService.printErrorMessage();
        }
	}

	private void viewPendingRequests() {
		// TODO Auto-generated method stub
        Transfer[] transfers = transferService.listPendingTransfers();
        if(transfers != null) {
            Transfer approveDeny = consoleService.printPendingRequests(transfers, currentUser.getUser().getUsername());
            if (approveDeny != null) {
                int choice = consoleService.approveOrDeny();
                Transfer updatedTransfer = transferService.approveOrDenyTransfer(choice, approveDeny);
                if (updatedTransfer != null) {
                    boolean success = transferService.updateTransfer(updatedTransfer);
                    if (success && choice == 1) {
                        System.out.println("Requested money sent!");
                    }
                    if (success && choice == 2) {
                        System.out.println("Requested amount rejected. Transfer has been closed.");
                    }
                }
            }
        }
		
	}

	private void sendBucks() {
		// TODO Auto-generated method stub
        User[] users = transferService.getAllUsers();
        Send send = consoleService.createSend(users, "send");
        if (send != null) {
            Transfer transfer = transferService.sendBucks(send);
            consoleService.displaySuccessfulSend(transfer);
        }
	}

	private void requestBucks() {
		// TODO Auto-generated method stub
        User[] users = transferService.getAllUsers();
        Send send = consoleService.createSend(users, "request");
        Transfer transfer = transferService.requestBucks(send);
        consoleService.displaySuccessfulRequest(transfer);
	}



}
