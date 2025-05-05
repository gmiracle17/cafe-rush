package com.caferush.game;

import java.util.Scanner;

/**
 * cmd version na magulo
 */
public class MachineThreads {

    // Abstract Machine class
    static abstract class Machine extends Thread {
        protected volatile boolean isBusy = false;
        protected String choice;
        protected final String name;

        public Machine(String name) {
            this.name = name;
        }

        public boolean startProcess(String item) {
            if (isBusy) {
                System.out.println(name + " is currently busy.");
                return false;
            } else {
                this.choice = item;
                this.isBusy = true;
                this.start();
                return true;
            }
        }

        @Override
        public void run() {
            process(choice);
            isBusy = false;
        }

        protected abstract void process(String item);
    }

    static class CoffeeMaker extends Machine {
        public CoffeeMaker(String name) {
            super(name);
        }

        @Override
        protected void process(String item) {
            System.out.println(name + " is making coffee: " + item);
            try {
                Thread.sleep(10000); // 10 seconds
            } catch (InterruptedException e) {
                System.out.println(name + " was interrupted.");
            }
            System.out.println(name + ": " + item + " is ready!");
        }
    }

    static class Oven extends Machine {
        public Oven(String name) {
            super(name);
        }

        @Override
        protected void process(String item) {
            System.out.println(name + " is baking: " + item);
            try {
                Thread.sleep(20000); // 20 seconds
            } catch (InterruptedException e) {
                System.out.println(name + " was interrupted.");
            }
            System.out.println(name + ": " + item + " is ready!");
        }
    }

    static class PastryMaker extends Machine {
        public PastryMaker(String name) {
            super(name);
        }

        @Override
        protected void process(String item) {
            System.out.println(name + " is preparing pastry: " + item);
            try {
                Thread.sleep(5000); // 5 seconds
            } catch (InterruptedException e) {
                System.out.println(name + " was interrupted.");
            }
            System.out.println(name + ": " + item + " is ready!");
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        String[] coffeeOptions = {"espresso", "americano", "hotchoco"};
        String[] ovenOptions = {"donut", "bread", "croissant"};
        String[] pastryOptions = {"biscuit", "crinkles", "cupcake", "shortcake"};

        Machine[] coffeeMakers = {
                new CoffeeMaker("Coffee Maker 1"),
                new CoffeeMaker("Coffee Maker 2"),
                new CoffeeMaker("Coffee Maker 3"),
                new CoffeeMaker("Coffee Maker 4")
        };

        Machine[] ovens = {
                new Oven("Oven 1"),
                new Oven("Oven 2")
        };

        Machine[] pastryMakers = {
                new PastryMaker("Pastry Maker 1"),
                new PastryMaker("Pastry Maker 2")
        };

        System.out.println("Welcome to the Multi-Machine Café!");
        System.out.println("Type 'exit' at any time to leave.");

        while (true) {
            System.out.println("\n--- Machine Menu ---");
            System.out.println("1. Coffee Makers:");
            for (int i = 0; i < coffeeMakers.length; i++)
                System.out.println("   [" + (i + 1) + "] " + coffeeMakers[i].name + " - " + (coffeeMakers[i].isBusy ? "BUSY" : "Available"));

            System.out.println("2. Ovens:");
            for (int i = 0; i < ovens.length; i++)
                System.out.println("   [" + (i + 1) + "] " + ovens[i].name + " - " + (ovens[i].isBusy ? "BUSY" : "Available"));

            System.out.println("3. Pastry Makers:");
            for (int i = 0; i < pastryMakers.length; i++)
                System.out.println("   [" + (i + 1) + "] " + pastryMakers[i].name + " - " + (pastryMakers[i].isBusy ? "BUSY" : "Available"));

            System.out.print("\nChoose machine type (coffee/oven/pastry) or 'exit': ");
            String type = scanner.nextLine().trim().toLowerCase();

            if (type.equals("exit")) break;

            Machine[] selectedMachines;
            String[] options;
            switch (type) {
                case "coffee":
                    selectedMachines = coffeeMakers;
                    options = coffeeOptions;
                    break;
                case "oven":
                    selectedMachines = ovens;
                    options = ovenOptions;
                    break;
                case "pastry":
                    selectedMachines = pastryMakers;
                    options = pastryOptions;
                    break;
                default:
                    System.out.println("Invalid machine type. Try again.");
                    continue;
            }

            System.out.print("Select machine number (1 to " + selectedMachines.length + "): ");
            int index;
            try {
                index = Integer.parseInt(scanner.nextLine()) - 1;
                if (index < 0 || index >= selectedMachines.length) {
                    System.out.println("Invalid number.");
                    continue;
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input.");
                continue;
            }

            Machine machine = selectedMachines[index];

            // Immediate busy check
            if (machine.isBusy) {
                System.out.println(machine.name + " is currently busy. Please choose a different machine.");
                continue;
            }

            System.out.print("Choose item to prepare " + optionsToString(options) + ": ");
            String item = scanner.nextLine().trim().toLowerCase();

            if (!arrayContains(options, item)) {
                System.out.println("Invalid item for this machine.");
                continue;
            }

            machine.startProcess(item);
        }

        System.out.println("\nGoodbye!");
        scanner.close();
    }

    private static boolean arrayContains(String[] array, String value) {
        for (String item : array) {
            if (item.equals(value)) return true;
        }
        return false;
    }

    private static String optionsToString(String[] options) {
        StringBuilder sb = new StringBuilder("(");
        for (int i = 0; i < options.length; i++) {
            sb.append(options[i]);
            if (i < options.length - 1) sb.append(", ");
        }
        sb.append(")");
        return sb.toString();
    }
}
