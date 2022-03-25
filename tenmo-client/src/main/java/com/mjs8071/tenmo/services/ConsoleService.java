package com.mjs8071.tenmo.services;


import com.mjs8071.tenmo.model.Send;
import com.mjs8071.tenmo.model.Transfer;
import com.mjs8071.tenmo.model.User;
import com.mjs8071.tenmo.model.UserCredentials;

import java.math.BigDecimal;
import java.util.Scanner;

public class ConsoleService {

    private final Scanner scanner = new Scanner(System.in);

    public int promptForMenuSelection(String prompt) {
        int menuSelection;
        System.out.print(prompt);
        try {
            menuSelection = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            menuSelection = -1;
        }
        return menuSelection;
    }

    public void printGreeting() {
        System.out.println("*********************");
        System.out.println("* Welcome to TEnmo! *");
        System.out.println("*********************");
    }

    public void printLoginMenu() {
        System.out.println();
        System.out.println("1: Register");
        System.out.println("2: Login");
        System.out.println("0: Exit");
        System.out.println();
    }

    public void printMainMenu() {
        System.out.println();
        System.out.println("1: View your current balance");
        System.out.println("2: View your past transfers");
        System.out.println("3: View your pending requests");
        System.out.println("4: Send TE bucks");
        System.out.println("5: Request TE bucks");
        System.out.println("0: Exit");
        System.out.println();
    }

    public UserCredentials promptForCredentials() {
        String username = promptForString("Username: ");
        String password = promptForString("Password: ");
        return new UserCredentials(username, password);
    }

    public String promptForString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    public int promptForInt(String prompt) {
        System.out.print(prompt);
        while (true) {
            try {
                return Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a number.");
            }
        }
    }

    public BigDecimal promptForBigDecimal(String prompt) {
        System.out.print(prompt);
        while (true) {
            try {
                return new BigDecimal(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a decimal number.");
            }
        }
    }

    public void pause() {
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }

    public void printErrorMessage() {
        System.out.println("An error occurred. Check the log for details.");
    }

    public void printTransfers(Transfer[] transfers, String username) {
        System.out.println("--------------------------------------------");
        System.out.println("Transfers");
        System.out.println("ID              From/To               Amount");
        System.out.println("--------------------------------------------");
        for (Transfer transfer : transfers) {
            String fromOrTo;
            String usernameFromOrTo;
            if(username.equals(transfer.getUsernameFrom())) {
                fromOrTo = "  To: ";
                usernameFromOrTo = transfer.getUsernameTo();
            } else {
                fromOrTo = "From: ";
                usernameFromOrTo = transfer.getUsernameFrom();
            }

            System.out.println(transfer.getTransferId() + "          " + fromOrTo + usernameFromOrTo + "        $" + transfer.getAmount());
        }
    }
    public Transfer printPendingRequests(Transfer[] transfers, String username) {
        System.out.println("--------------------------------------------");
        System.out.println("Viewing all requests From or To: " + username);
        System.out.println("Transfers");
        System.out.println("ID              Requester/Received a request               Amount");
        System.out.println("--------------------------------------------");
        for (Transfer transfer : transfers) {
            String fromOrTo;
            String usernameFromOrTo;
            if(username.equals(transfer.getUsernameTo())) {
                fromOrTo = " Received your request for: ";
                usernameFromOrTo = transfer.getUsernameFrom();
            } else {
                fromOrTo = "  REQUESTS THAT YOU SEND: ";
                usernameFromOrTo = transfer.getUsernameTo();
            }

            System.out.println(transfer.getTransferId() + "          " + usernameFromOrTo + fromOrTo + "        $" + transfer.getAmount());
        }
        System.out.println("Please enter transfer ID to approve/reject (0 to cancel): ");
        Transfer returningTransfer = null;
        boolean check = true;
        while (check) {
            String userInput = scanner.nextLine();
            if (userInput.equals("0")) {
                break;
            }
            Long transferId;
            try {
                transferId = Long.valueOf(userInput);
            } catch (NumberFormatException e) {
                continue;
            }
            for (Transfer transfer : transfers) {
                if (transfer.getTransferId().equals(transferId) && transfer.getUsernameFrom().equals(username)) {
                    returningTransfer = transfer;
                    check = false;
                    break;
                }
            }
            System.out.println("Incorrect Entry. Please try again.");
        }
        return returningTransfer;
    }

    public int approveOrDeny() {
        int choice = 0;
        while (true) {
            System.out.println("1: Approve");
            System.out.println("2: Reject");
            System.out.println("0: Don't Approve or Reject");
            System.out.println("------");
            System.out.println("Please choose an option:");
            String userInput = scanner.nextLine();
            if (userInput.equals("0")) {
                return choice;
            } else if (userInput.equals("1")) {
                choice = 1;
                return choice;
            } else if (userInput.equals("2")) {
                choice = 2;
                return choice;
            } else {
                System.out.println("Incorrect entry, please enter 0, 1, or 2.");
            }
        }
    }

    public Transfer pickTransferFromList(Transfer[] transfers) {
        Transfer transferToReturn = null;
        System.out.println("Please enter transfer ID to view details (0 to cancel): ");
        while (true) {
            String userInput = scanner.nextLine();
            if (userInput.equals("0")) {
                break;
            }
            Long transferId;
            try {
                transferId = Long.valueOf(userInput);
            } catch (NumberFormatException e) {
                continue;
            }
            for (Transfer transfer : transfers) {
                if (transfer.getTransferId().equals(transferId)) {
                    return transfer;
                }
            }
            System.out.println("Transfer not found. Please try again.");
        }
        return transferToReturn;
    }

    public void displayTransfer(Transfer transfer) {
        System.out.println("--------------------------------------------");
        System.out.println("Transfer Details");
        System.out.println("--------------------------------------------");
        System.out.println("ID: " + transfer.getTransferId());
        System.out.println("From: " + transfer.getUsernameFrom());
        System.out.println("To: " + transfer.getUsernameTo());
        System.out.println("Type: " + transfer.getTransferTypeDesc());
        System.out.println("Status: " + transfer.getTransferStatusDesc());
        System.out.println("Amount: $" + transfer.getAmount());
    }

    public void printCurrentBalance(BigDecimal balance) {
        System.out.println("Your current account balance is: $" + balance);
    }

    public Send createSend(User[] users, String type) {
        displayUsers(users);
        if(type.equals("send")) {
            System.out.println("Enter the username of the user you are sending to (0 to cancel): ");
        } else {
            System.out.println("Enter the username of the user you would like to send a request to: ");
        }
        Send send = new Send();
        String username = scanner.nextLine();

        boolean check = false;
        for (User user : users) {
            if (username.equals(user.getUsername())){
                check = true;
            }
        }
        if (username.equals("0") || !check) {
            send = null;
            System.out.println("Exiting send menu...");
            return send;
        }
        System.out.println("Enter amount: ");
        String value = scanner.nextLine();
        BigDecimal amount = new BigDecimal(value);
        send.setAmount(amount);
        send.setUsernameTo(username);
        return send;
    }

    public void displayUsers(User[] users) {
        System.out.println("--------------------------------------------");
        System.out.println("Users");
        System.out.println("ID          Name");
        System.out.println("--------------------------------------------");
        for (User user : users) {
            System.out.println(user.getId() + "           " + user.getUsername());
        }
        System.out.println("--------");
    }

    public void displaySuccessfulSend(Transfer transfer) {
        System.out.println("The transaction was successful.");
        System.out.println("Transfer ID: " + transfer.getTransferId());
        System.out.println("User: " + transfer.getUsernameTo() + " received: $" + transfer.getAmount());
    }

    public void displaySuccessfulRequest(Transfer transfer) {
        System.out.println("Request sent.");
        System.out.println("Transfer ID: " + transfer.getTransferId());
        System.out.println("User: " + transfer.getUsernameFrom() + " received a request for: $" + transfer.getAmount());
        System.out.println("Your transfer status is pending.");
    }



}
