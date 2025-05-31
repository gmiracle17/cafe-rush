
<p style="display: flex; align-items: flex-start; gap: 10px;">
  <img src="lwjgl3/src/main/resources/libgdx64.png" alt="Cafe Rush" height="40" style="margin-top: 10px;">
  <span style="font-size: 2em; font-weight: bold; margin-top: 10px;">Cafe Rush</span>
</p>

Café Rush is a fast-paced, time-management simulator built with [libGDX](https://libgdx.com/) and generated with [gdx-liftoff](https://github.com/libgdx/gdx-liftoff)

The game places players in the shoes of a café owner who must skillfully juggle taking orders, preparing drinks, baking treats, and managing customers, all while ensuring that everything runs smoothly in a chaotic and dynamic café environment. The game uses multithreading to simulate the simultaneous management of various café operations, offering a rich, immersive gameplay experience.

This project was generated with a template including simple application launchers and an `ApplicationAdapter` extension that draws libGDX logo.


## ✨ Features

- Multithreaded game simulation
- Animated cat-waiter characters
- Unique customer moods and patience levels
- Interactive machines: CoffeeMaker, Oven, PastryMaker
- Background music and sound effects
## 💻 Run Locally

Clone the project

```bash
  git clone https://github.com/gmiracle17/cafe-rush.git
```

Go to the project directory

```bash
  cd cafe-rush
```

Import the project into your IDE (ideally IntelliJ)

    1. Open IntelliJ IDEA
    2. Select "Import Project"
    3. Choose Gradle as the import method

Edit configurations

    1. Go to Run > Edit Configurations...
    2. Click the + button and choose Application
    3. Set the following fields:
        - Name: Cafe Rush
        - Main class: com.caferush.game.lwjgl3.DesktopLauncher
        - Classpath: cafe-rush.lwjgl3.main
        - JDK: Select Java 21

## 🛠 Tech Stack

- **Framework:** LibGDX
- **Language:** Java 21


## 📂 Code Structure

The game uses multithreading with Machines, Customers, and Orders having separate threads to handle their different timers and behavior


<u>**Machine Threads**</u>

The `Machines` class handles all machine threading and logic in Cafe Rush, managing the behavior and visual feedback of three different types of in-game machines (CoffeeMaker, Oven, PastryMaker).

Each machine operates as its own thread and is responsible for:

- Displaying interactive options on the tiled map
- Processing an item over time
- Showing processing status (Red, Yellow, Green)
- Pausing/resuming work
- Displaying completed orders
- Allowing order collection into the inventory

This system is visualized on the game's Tiled map through custom tile layers and triggered by proximity or user clicks.

```javascript
import Component from 'my-project'

function App() {
  return <Component />
}
```

**Customer Spawner Thread**

This thread, found in `CustomerHandler` is responsible for the generation of customers for the cafe.

- *Random Timing*: Within the thread, there is a random delay (3-8 seconds) between the spawning of customers to give a sense of unpredictability. 
- *Population Control*: Enforces a specific number of customers that can  be handled at once (8 that can be seated, and 1 that is waiting to be seated)
- *Spawn Validation*: Before spawning a customer, the following conditions must be met: 
  - No customer is waiting at the spawn point
  - The number of customers spawned is below the maximum number of customers set as part of the population control

**Customer Patience Thread**

This thread is responsible for managing the patience timer for each customer which determines their waiting behavior

- Controls patience timers for different customer states: Manages timing between `spawn` (waiting for seat) and `seated` (waiting for order) phases 
- Thread Communication: Timer threads directly modify customer state and trigger patience loss when timers expire

## 🌐 Platforms

- `core`: Main module with the application logic shared by all platforms.
- `lwjgl3`: Primary desktop platform using LWJGL3; was called 'desktop' in older docs.

## ⚙️ Gradle

This project uses [Gradle](https://gradle.org/) to manage dependencies.
The Gradle wrapper was included, so you can run Gradle tasks using `gradlew.bat` or `./gradlew` commands.
Useful Gradle tasks and flags:

- `--continue`: when using this flag, errors will not stop the tasks from running.
- `--daemon`: thanks to this flag, Gradle daemon will be used to run chosen tasks.
- `--offline`: when using this flag, cached dependency archives will be used.
- `--refresh-dependencies`: this flag forces validation of all dependencies. Useful for snapshot versions.
- `build`: builds sources and archives of every project.
- `cleanEclipse`: removes Eclipse project data.
- `cleanIdea`: removes IntelliJ project data.
- `clean`: removes `build` folders, which store compiled classes and built archives.
- `eclipse`: generates Eclipse project data.
- `idea`: generates IntelliJ project data.
- `lwjgl3:jar`: builds application's runnable jar, which can be found at `lwjgl3/build/libs`.
- `lwjgl3:run`: starts the application.
- `test`: runs unit tests (if any).

Note that most tasks that are not specific to a single project can be run with `name:` prefix, where the `name` should be replaced with the ID of a specific project.
For example, `core:clean` removes `build` folder only from the `core` project.


## 🕹️ How to Play
Press WASD to control waiter cat

When customers arrive at the doorstep, click them, drag and drop them to their seats before their patience run out

Customer's orders only appear after they have seaten

Go near machines and press `[SPACE]` to create products

    1. Near Coffee Makers to create hot chocolate, espresso, or americano

    2. Near Pastry Maker to create biscuits, shortcake, cupcakes, or crinkles

    3. Near Ovens to create croissants, bread, or donuts

Choose product to create and wait for them to be prepared

Collect products once their prepared and deliver them to the customers before it is too late!

If your inventory is full and you need to throw away products, go near the trashbin 
## 📄 References

Asset Pack References 🎨 

| Asset Pack | Usage |
|------------|-------|
| [Neko Cafe Asset Pack](https://hellorumin.itch.io/neko-cafe-asset-pack) | Background, Cat Characters, Machines |
| [Bar Katto](https://ivoryred.itch.io/bar-katto-16x16-icon-pack) | Snacks and Beverages |
| [Food and little bit of kitchenware](https://piiixl.itch.io/food) | Trashbin |
| [Modern Interiors](https://limezu.itch.io/moderninteriors) | Inventory and Boxes |

SFX References 🔊 

| Sound | Usage |
|-------|-------|
| [Cat Meow 8 FX](https://pixabay.com/sound-effects/cat-meow-8-fx-306184/) | Successful Delivery of Order |
| [Cat Meow 1 FX](https://pixabay.com/sound-effects/cat-meow-1-fx-306178/) | Customer Lost Patience |
| [Ding](https://pixabay.com/sound-effects/ding-101492/) | Product Ready |
| [Pop](https://pixabay.com/sound-effects/pop-39222/) | Customer Arrived |
| [Blue Archive OST 47. Coffee Cats](https://www.youtube.com/watch?v=6iB1JvaE7_E) | Cafe BGM |

Game Inspo 🎮 

| Game | Description |
|------|-------------|
| [Katito Cafe](https://lusitano.itch.io/katito-cafe) | A cat café game by Lusitano made with Unity |
| [Papa's Pizzeria](https://www.crazygames.com/game/papas-pizzeria) | A classic time-management cooking game |

## 👥 Authors

- Gichelle Miracle C. Burwell - [@gmiracle17](https://www.github.com/gmiracle17)
- Maria Amor G. Eco - [@mag-eco](https://github.com/mag-eco)
- Brian Cedrick R. Lao - [@Lao02](https://github.com/Lao02)

with guidance from Reuben Cabrera [@rjcabrera158](https://github.com/rjcabrera158)