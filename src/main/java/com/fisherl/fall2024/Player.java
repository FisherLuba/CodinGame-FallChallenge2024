// package is commented out so I can copy and paste to the IDE in CodinGame

//package com.fisherl.fall2024;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * All classes have to be in the same file for CodinGame, and the main class has to be named Player.
 */

final class Util {

    private static final double EPSILON = 0.0000001;

    // provided by CodinGame
    public static double distance(Point p1, Point p2) {
        return p1.distance(p2);
    }

    public static boolean pointOnSegment(Point building, Point segmentStart, Point segmentEnd) {
        final double distance = distance(segmentStart, building) + distance(building, segmentEnd) - distance(segmentStart, segmentEnd);
        return -EPSILON < distance && distance < EPSILON;
    }

    public static double orientation(Point p1, Point p2, Point p3) {
        final double prod = (p3.y() - p1.y()) * (p2.x() - p1.x()) - (p2.y() - p1.y()) * (p3.x() - p1.x());
        return prod >= 0 ? 1 : -1;
    }

    public static boolean segmentsIntersect(Point a, Point b, Point c, Point d) {
        return orientation(a, b, c) * orientation(a, b, d) < 0 && orientation(c, d, a) * orientation(c, d, b) < 0;
    }

}

final class Prices {

    public static final double TUBE = 0.1; // per kilometer
    public static final int TELEPORTER = 5_000; // static cost
    public static final int POD = 1_000; // static cost

    /**
     * @return - the cost of the transporter rounded down
     */
    public static double getCost(TransporterType type, Building from, Building to) {
        return switch (type) {
            case TUBE -> {
                final double distance = from.point().distance(to.point());
                yield Math.floor(distance / TUBE);
            }
            case TELEPORTER -> TELEPORTER;
            default -> throw new IllegalArgumentException("Unknown transporter type: " + type);
        };
    }

}


final class Player {

    private static boolean running = true;

    public static void main(String[] args) {
        final Scanner scanner = new Scanner(System.in);

        final Game game = new Game(0, new HashMap<>(), new HashMap<>());

        // game loop
        while (running) {
            final InputData inputData = InputHelper.readInput(scanner);

//            System.err.println(inputData);
            inputData.buildingDescriptions()
                    .stream()
                    .map(BuildingDescription::create)
                    .forEach(game::addBuilding);

            game.resourceCount(inputData.resources());
            game.takeTurn();
        }
    }

}

final class InputHelper {

    private InputHelper() {
        throw new IllegalStateException("This class should not be instantiated");
    }

    public static InputData readInput(Scanner scanner) {
        final List<TravelRoute> travelRoutes = new ArrayList<>();
        final List<PodDescription> podDescriptions = new ArrayList<>();
        final List<BuildingDescription> buildingDescriptions = new ArrayList<>();

        final int resources = scanner.nextInt();
//        System.err.println("Resources: " + resources);

        final int numTravelRoutes = scanner.nextInt();
//        System.err.println("numTravelRoutes: " + numTravelRoutes);
        for (int i = 0; i < numTravelRoutes; i++) {
            travelRoutes.add(readTravelRoute(scanner));
        }

        final int numPods = scanner.nextInt();
//        System.err.println("numPods: " + numPods);
        for (int i = 0; i < numPods; i++) {
            podDescriptions.add(readPodDescription(scanner));
        }

        final int numNewBuildings = scanner.nextInt();
//        System.err.println("numNewBuildings: " + numNewBuildings);
        for (int i = 0; i < numNewBuildings; i++) {
            buildingDescriptions.add(readBuildingDescription(scanner));
        }

        return new InputData(resources, numTravelRoutes, travelRoutes, numPods, podDescriptions, numNewBuildings, buildingDescriptions);
    }


    private static TravelRoute readTravelRoute(Scanner scanner) {
        final int buildingId1 = scanner.nextInt();
        final int buildingId2 = scanner.nextInt();
        final int capacity = scanner.nextInt();
//        System.err.printf("buildingId1: %d, building2: %d, capacity: %d\n", buildingId1, buildingId2, capacity);
        return new TravelRoute(buildingId1, buildingId2, capacity);
    }

    private static PodDescription readPodDescription(Scanner scanner) {
        final int uniqueIdentifier = scanner.nextInt();
        final int numStops = scanner.nextInt();
        final List<Integer> buildingStops = new ArrayList<>();
        for (int j = 0; j < numStops; j++) {
            final int buildingStop = scanner.nextInt();
            buildingStops.add(buildingStop);
        }
//        System.err.printf(
//                "uniqueIdentifier: %d, numStops: %d, buildingStops: %s\n",
//                uniqueIdentifier,
//                numStops,
//                buildingStops.stream().map(String::valueOf).collect(Collectors.joining(","))
//        );
        return new PodDescription(uniqueIdentifier, numStops, buildingStops);
    }

    private static BuildingDescription readBuildingDescription(Scanner scanner) {
        final int moduleType = scanner.nextInt();
        final int buildingId = scanner.nextInt();
        final int coordX = scanner.nextInt();
        final int coordY = scanner.nextInt();

        if (moduleType == 0) {
            final int numAstronauts = scanner.nextInt();
            final List<Integer> astronauts = new ArrayList<>();
            for (int j = 0; j < numAstronauts; j++) {
                final int astronaut = scanner.nextInt();
                astronauts.add(astronaut);
            }
//            System.err.printf(
//                    "moduleType: %d, buildingId: %d, coordX: %d, coordY: %d, numAstronauts: %d, astronauts: %s\n",
//                    moduleType,
//                    buildingId,
//                    coordX,
//                    coordY,
//                    numAstronauts,
//                    astronauts.stream().map(String::valueOf).collect(Collectors.joining(","))
//            );
            return new LandingPadDescription(buildingId, coordX, coordY, astronauts);
        }
//        System.err.printf(
//                "moduleType: %d, buildingId: %d, coordX: %d, coordY: %d\n",
//                moduleType,
//                buildingId,
//                coordX,
//                coordY
//        );
        return new LunarModuleDescription(moduleType, buildingId, coordX, coordY);
    }

}

class TravelRoute {

    private final int buildingId1;
    private final int buildingId2;
    private int capacity;

    public TravelRoute(int buildingId1, int buildingId2, int capacity) {
        this.buildingId1 = buildingId1;
        this.buildingId2 = buildingId2;
        this.capacity = capacity;
    }

    public int buildingId1() {
        return this.buildingId1;
    }

    public int buildingId2() {
        return this.buildingId2;
    }

    public int capacity() {
        return this.capacity;
    }

    public void capacity(int capacity) {
        this.capacity = capacity;
    }

}

record PodDescription(int uniqueId, int numStops, List<Integer> buildingStops) {

}

interface BuildingDescription {

    int moduleType();

    int buildingId();

    int coordX();

    int coordY();

    Building create();

}

record LandingPadDescription(
        int buildingId,
        int coordX,
        int coordY,
        List<Integer> astronautTypes
) implements BuildingDescription {

    @Override
    public Building create() {
        return new LandingPad(this.buildingId, new Point(this.coordX, this.coordY), new HashMap<>(), this.astronautTypes);
    }

    @Override
    public int moduleType() {
        return 0;
    }
}

record LunarModuleDescription(int moduleType, int buildingId, int coordX, int coordY) implements BuildingDescription {

    @Override
    public Building create() {
        return new LunarModule(this.buildingId, new Point(this.coordX, this.coordY), this.moduleType, new HashMap<>());
    }

}

record InputData(int resources, int numTravelRoutes, List<TravelRoute> travelRoutes, int numPods,
                 List<PodDescription> podDescriptions, int numNewBuildings,
                 List<BuildingDescription> buildingDescriptions) {

}


enum BuildingType {

    LANDING_PAD,
    LUNAR_MODULE

}

abstract class Building {

    public static final int MAX_TUBES = 5;

    private final BuildingType type;
    private final int id;
    private final Point point;
    // can be null
    private Teleporter teleporter;
    // key is the id of the building this building connects to
    private final Map<Integer, Tube> tubesTo;

    public Building(BuildingType type, int id, Point point, Map<Integer, Tube> tubesTo) {
        this.type = type;
        this.id = id;
        this.point = point;
        this.tubesTo = tubesTo;
    }

    public BuildingType type() {
        return this.type;
    }

    public int id() {
        return this.id;
    }

    public Point point() {
        return this.point;
    }

    public void addTubeTo(Tube tube) {
        if (this.id == tube.building2Id()) {
            this.tubesTo.put(tube.building1Id(), tube);
            return;
        }
        if (this.id == tube.building1Id()) {
            this.tubesTo.put(tube.building2Id(), tube);
            return;
        }
        throw new IllegalStateException("Tube does not connect to this building");
    }

    /**
     * @return possibly null {@link Teleporter}
     */
    public Teleporter teleporter() {
        return this.teleporter;
    }

    public void teleporter(Teleporter teleporter) {
        if (this.id != teleporter.fromId() && this.id != teleporter.toId()) {
            throw new IllegalArgumentException("Teleporter does not connect to this building");
        }
        this.teleporter = teleporter;
    }

    public Map<Integer, Tube> viewTubesTo() {
        return Collections.unmodifiableMap(this.tubesTo);
    }

    public boolean hasMaxTubes() {
        return this.tubesTo.size() >= MAX_TUBES;
    }

}

class LunarModule extends Building {

    private final int moduleType;

    public LunarModule(int id, Point point, int moduleType, Map<Integer, Tube> tubesTo) {
        super(BuildingType.LUNAR_MODULE, id, point, tubesTo);
        this.moduleType = moduleType;
    }

    public int moduleType() {
        return this.moduleType;
    }

}

class LandingPad extends Building {

    private final List<Integer> astronauts;
    private final TreeMap<Integer, Integer> astronautCounts;
    private final TreeMap<Integer, List<Integer>> countsToAstronauts;

    public LandingPad(
            int id,
            Point point,
            Map<Integer, Tube> tubesTo,
            List<Integer> astronauts
    ) {
        super(BuildingType.LANDING_PAD, id, point, tubesTo);
        this.astronauts = astronauts;
        this.astronautCounts = new TreeMap<>();
        this.countsToAstronauts = new TreeMap<>();
        for (int astronaut : astronauts) {
            this.astronautCounts.put(astronaut, this.astronautCounts.getOrDefault(astronaut, 0) + 1);
        }
        for (Map.Entry<Integer, Integer> entry : this.astronautCounts.entrySet()) {
            final int astronaut = entry.getKey();
            final int count = entry.getValue();
            this.countsToAstronauts.computeIfAbsent(count, key -> new ArrayList<>()).add(astronaut);
        }
    }

    public List<Integer> viewAstronauts() {
        return Collections.unmodifiableList(this.astronauts);
    }

    public int countAstronauts(int type) {
        return this.astronautCounts.getOrDefault(type, 0);
    }

    public int mostCommonAstronaut() {
        return this.countsToAstronauts.lastEntry().getValue().get(0); // should never be empty
    }

    public Set<Integer> commonAstronautsInOrder() {
        final Set<Integer> mostCommon = new LinkedHashSet<>();

        for (Map.Entry<Integer, List<Integer>> entry : this.countsToAstronauts.descendingMap().entrySet()) {
            final List<Integer> astronauts = entry.getValue();
            mostCommon.addAll(astronauts);
        }
        return mostCommon;
    }

}

enum TransporterType {

    TUBE,
    TELEPORTER

}

sealed interface Transporter permits Teleporter, Tube {

    TransporterType type();

}

final class Teleporter implements Transporter {

    private final int fromId;
    private final int toId;

    public Teleporter(int fromId, int toId) {
        this.fromId = fromId;
        this.toId = toId;
    }

    @Override
    public TransporterType type() {
        return TransporterType.TELEPORTER;
    }

    public int fromId() {
        return this.fromId;
    }

    public int toId() {
        return this.toId;
    }

}

final class Tube implements Transporter {

    private final int building1Id;
    private final int building2Id;
    private int capacity = 0;

    public Tube(int building1Id, int building2Id) {
        this.building1Id = building1Id;
        this.building2Id = building2Id;
    }

    @Override
    public TransporterType type() {
        return TransporterType.TUBE;
    }

    public int capacity() {
        return this.capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int building1Id() {
        return this.building1Id;
    }

    public int building2Id() {
        return this.building2Id;
    }

}

class Pod {

    private final PodDescription podDescription;
    private int currentBuilding;

    public Pod(PodDescription podDescription, int currentBuilding) {
        this.podDescription = podDescription;
        this.currentBuilding = currentBuilding;
    }

    public int currentBuilding() {
        return this.currentBuilding;
    }

    public void currentBuilding(int currentBuilding) {
        this.currentBuilding = currentBuilding;
    }

    public PodDescription podDescription() {
        return this.podDescription;
    }
}

class Game {

    public static final int MAX_POD_ID = 500;
    private int currentPodId = MAX_POD_ID;

    private int resourceCount;
    private final Map<Integer, Building> buildingsById;
    private final Map<Integer, Pod> podsById;
    private final Map<Integer, Set<LunarModule>> lunarModulesByType;

    public Game(int resourceCount, Map<Integer, Building> buildingsById, Map<Integer, Pod> podsById) {
        this.resourceCount = resourceCount;
        this.buildingsById = buildingsById;
        this.podsById = podsById;
        this.lunarModulesByType = new HashMap<>();
    }

    public void addBuilding(Building building) {
        this.buildingsById.put(building.id(), building);
        if (building instanceof final LunarModule lunarModule) {
            this.lunarModulesByType.computeIfAbsent(lunarModule.moduleType(), key -> new HashSet<>()).add(lunarModule);
        }
    }

    public void addPod(Pod pod) {
        this.podsById.put(pod.podDescription().uniqueId(), pod);
    }

    // very naive implementation, I know this is very inefficient
    public void takeTurn() {
        final StringBuilder command = new StringBuilder();
        this.podsById.entrySet().removeIf(entry -> {
            final Pod pod = entry.getValue();
            final int currentBuilding = pod.currentBuilding();
            boolean previousWasCurrent = false;
            final List<Integer> stops = pod.podDescription().buildingStops();
            for (int buildingId : stops) {
                if (previousWasCurrent) {
                    pod.currentBuilding(buildingId);
                    return false;
                }
                if (currentBuilding == buildingId) {
                    previousWasCurrent = true;
                }
            }
            if (previousWasCurrent && !Objects.equals(stops.get(0), stops.get(stops.size() - 1))) {
                command.append("DESTROY ").append(pod.podDescription().uniqueId()).append(";");
                return true;
            }
            return false;
        });

        final Collection<LandingPad> landingPads = this.getLandingPads()
                .filter(landingPad -> landingPad.viewTubesTo().isEmpty())
                .sorted((first, second) -> {
                    final int firstMostCommon = first.mostCommonAstronaut();
                    final int secondMostCommon = second.mostCommonAstronaut();
                    return Integer.compare(first.countAstronauts(firstMostCommon), second.countAstronauts(secondMostCommon));
                })
                .toList();
        for (LandingPad landingPad : landingPads) {
            if (landingPad.hasMaxTubes()) {
                continue;
            }
//            System.err.println("Landing pad: " + landingPad.id());
            for (int astronautType : landingPad.commonAstronautsInOrder()) {
//                System.err.println("Astronaut type: " + astronautType);
//                final int mostCommon = landingPad.mostCommonAstronaut();
                final Collection<LunarModule> lunarModules = this.lunarModulesByType.getOrDefault(astronautType, Collections.emptySet());
                if (lunarModules.isEmpty()) {
                    continue;
                }
                for (LunarModule lunarModule : lunarModules) {
                    if (landingPad.hasMaxTubes()) {
                        break;
                    }
                    if (lunarModule.hasMaxTubes()) {
                        continue;
                    }
//                    System.err.println("What??? " + landingPad.hasMaxTubes() + " " + lunarModule.hasMaxTubes());
//                    System.err.println("Lunar module: " + lunarModule.id());
                    if (lunarModule.viewTubesTo().containsKey(landingPad.id())) {
                        continue;
                    }
                    final Tube newTube = new Tube(lunarModule.id(), landingPad.id());
                    if (this.intersectsBuilding(lunarModule.id(), landingPad.id(), lunarModule.point(), landingPad.point())) {
                        continue;
                    }
                    if (this.intersectsSegment(lunarModule.id(), landingPad.id(), lunarModule.point(), landingPad.point())) {
                        continue;
                    }
                    final int cost = (int) Prices.getCost(TransporterType.TUBE, landingPad, lunarModule);
                    if (cost > this.resourceCount) {
                        continue;
                    }
                    this.resourceCount -= cost;
                    newTube.setCapacity(1);
                    lunarModule.addTubeTo(newTube);
                    landingPad.addTubeTo(newTube);
                    command.append("TUBE ").append(lunarModule.id()).append(" ").append(landingPad.id()).append(";");
                    System.err.println("Tube: " + lunarModule.id() + ", " + landingPad.id() + " sizes: " + landingPad.viewTubesTo().size() + ", " + lunarModule.viewTubesTo().size() + " | " + landingPad.hasMaxTubes() + ", " + lunarModule.hasMaxTubes());
                }
            }
        }

        final Set<LunarModule> lunarModules = this.getLunarModules().collect(Collectors.toSet());
        for (LunarModule lunarModule : lunarModules) {

            for (LunarModule lunarModule2 : lunarModules) {
                if (lunarModule.hasMaxTubes()) {
                    break;
                }
                if (lunarModule2.hasMaxTubes()) {
                    continue;
                }
                if (!lunarModule.equals(lunarModule2) && !lunarModule.viewTubesTo().containsKey(lunarModule2.id())) {
                    final Tube newTube = new Tube(lunarModule.id(), lunarModule2.id());
                    if (this.intersectsBuilding(lunarModule.id(), lunarModule2.id(), lunarModule.point(), lunarModule2.point())) {
                        continue;
                    }
                    if (this.intersectsSegment(lunarModule.id(), lunarModule2.id(), lunarModule.point(), lunarModule2.point())) {
                        continue;
                    }
                    final int cost = (int) Prices.getCost(TransporterType.TUBE, lunarModule, lunarModule2);
                    if (cost > this.resourceCount) {
                        continue;
                    }
                    this.resourceCount -= cost;
                    newTube.setCapacity(1);
                    lunarModule.addTubeTo(newTube);
                    lunarModule2.addTubeTo(newTube);
                    command.append("TUBE ").append(lunarModule.id()).append(" ").append(lunarModule2.id()).append(";");
                    System.err.println("Tube: " + lunarModule.id() + ", " + lunarModule2.id() + " sizes: " + lunarModule.viewTubesTo().size() + ", " + lunarModule2.viewTubesTo().size() + " | " + lunarModule.hasMaxTubes() + ", " + lunarModule2.hasMaxTubes());
                }
            }
        }

//        if (this.resourceCount >= Prices.POD && !connectedBuildings.isEmpty()) {
//            command.append("POD").append(" ").append(this.currentPodId--);
//            for (int buildingId : connectedBuildings) {
//                command.append(" ").append(buildingId);
//            }
//            command.append(";");
//            this.resourceCount -= Prices.POD;
//        }
        boolean placedPod = false;
        if (this.resourceCount >= Prices.POD && this.podsById.isEmpty() && !this.buildingsById.isEmpty()) {
            final List<Building> buildings = new ArrayList<>(this.buildingsById.values());
            Collections.shuffle(buildings);


            Building previousBuilding = null;


//            for (int i = 0; i < buildings.size() - 1; i++) {
//                for (int j = i + 1; j < buildings.size(); j++) {
            final Queue<Building> buildingQueue = new LinkedList<>();
            buildingQueue.add(buildings.get(0));
            int loopCount = 0;
            // I know this is stupid
            while (!buildingQueue.isEmpty()) {
                if (loopCount++ > this.buildingsById.size()) {
                    break;
                }
                final Building currentBuilding = buildingQueue.poll();
                final List<Map.Entry<Integer, Tube>> entries = new ArrayList<>(currentBuilding.viewTubesTo().entrySet());
                Collections.shuffle(entries);
                while (!entries.isEmpty()) {
                    final Map.Entry<Integer, Tube> entry = entries.remove(0);
                    final int buildingId = entry.getKey();
                    final Building building = this.buildingsById.get(buildingId);
                    if (building.equals(currentBuilding)) {
                        continue;
                    }
                    if (previousBuilding != null && !previousBuilding.viewTubesTo().containsKey(buildingId)) {
                        continue;
                    }
                    if (!placedPod) {
                        command.append("POD ").append(this.currentPodId--).append(" ").append(currentBuilding.id());
                        placedPod = true;
                    }
                    command.append(" ").append(building.id());
                    previousBuilding = currentBuilding;
                    buildingQueue.add(building);
                    break;
//                    if (previousBuilding != -1 && !building.viewTubesTo().containsKey(previousBuilding)) {
//                        continue;
//                    }
//                    if (building.viewTubesTo().containsKey(currentBuilding.id())) {
//                        if (!placedPod) {
//                            command.append("POD ").append(this.currentPodId--);
//                            placedPod = true;
//                        }
//                        command.append(" ").append(currentBuilding.id()).append(" ").append(building.id());
//                        previousBuilding = building.id();
//                        System.err.println("Pod: " + currentBuilding.id() + ", " + building.id());
//                    }
                }
//                for (Map.Entry<Integer, Tube> entry : currentBuilding.viewTubesTo().entrySet()) {
//                    final int buildingId = entry.getKey();
//                    final Building building = this.buildingsById.get(buildingId);
//                    buildingQueue.add(building);
//                    if (building.equals(building2)) {
//                        continue;
//                    }
//                    if (previousBuilding != -1 && building.viewTubesTo().containsKey(previousBuilding)) {
//                        continue;
//                    }
//                    if (building.viewTubesTo().containsKey(building2.id())) {
//                        if (!placedPod) {
//                            command.append("POD ").append(this.currentPodId--);
//                            placedPod = true;
//                        }
//                        command.append(" ").append(building.id()).append(" ").append(building2.id());
//                        previousBuilding = building2.id();
//                        System.err.println("Pod: " + building.id() + ", " + building2.id());
//                    }
//                }
//                }
//            }
            }

            if (placedPod) {
                command.append(";");
            }
            this.resourceCount -= Prices.POD;
        }

        if (command.isEmpty()) {
            command.append("WAIT;");
        }
        System.out.println(command);
    }

    private boolean intersectsSegment(int firstBuildingId, int secondBuildingId, Point a, Point b) {
        for (Building building : this.buildingsById.values()) {
            for (Tube connection : building.viewTubesTo().values()) {
                final Point first = this.buildingsById.get(connection.building1Id()).point();
                final Point second = this.buildingsById.get(connection.building2Id()).point();
                // only need to check one direction
                if (connection.building1Id() == building.id()) {
                    continue;
                }
//                if (firstBuildingId == connection.building1Id() && secondBuildingId == connection.building2Id()) {
//                    continue;
//                }
//                if (firstBuildingId == connection.building2Id() && secondBuildingId == connection.building1Id()) {
//                    continue;
//                }
                if (Util.segmentsIntersect(a, b, first, second)) {
//                    System.err.println("Intersects segment: (" + firstBuildingId + ", " + secondBuildingId + "), (" + connection.building1Id() + ", " + connection.building2Id() + ")");
                    return true;
                }
            }
        }
        return false;
    }

    private boolean intersectsBuilding(int firstBuildingId, int secondBuildingId, Point a, Point b) {
        for (Building building : this.buildingsById.values()) {
            if (building.id() == firstBuildingId || building.id() == secondBuildingId) {
//                System.err.println("Building is one of the two: (" + firstBuildingId + ", " + secondBuildingId + "), " + building.id());
                continue;
            }
            if (Util.pointOnSegment(building.point(), a, b)) {
//                System.err.println("Intersects building: (" + firstBuildingId + ", " + secondBuildingId + "), " + building.id());
                return true;
            }
//            System.err.println("Does not intersect building: (" + firstBuildingId + ", " + secondBuildingId + "), " + building.id());
        }
        return false;
    }

    public Stream<Building> getBuildingsByType(BuildingType type) {
        return this.buildingsById.values()
                .stream()
                .filter(building -> building.type() == type);
    }

    public <B extends Building> Stream<B> getBuildingsByType(Class<B> type) {
        return this.buildingsById.values()
                .stream()
                .filter(type::isInstance)
                .map(type::cast);
    }

    public Stream<LandingPad> getLandingPads() {
        return this.getBuildingsByType(LandingPad.class);
    }

    public Stream<LunarModule> getLunarModules() {
        return this.getBuildingsByType(LunarModule.class);
    }

    public void resourceCount(int resourceCount) {
        this.resourceCount = resourceCount;
    }

}

record Point(int x, int y) {

    public Point add(int addX, int addY) {
        return new Point(this.x + addX, this.y + addY);
    }

    public Point add(int add) {
        return this.add(add, add);
    }

    public Point sub(int subX, int subY) {
        return this.add(-subX, -subY);
    }

    public Point sub(int sub) {
        return add(-sub);
    }

    public Point scale(int scaleX, int scaleY) {
        return new Point(this.x * scaleX, this.y * scaleY);
    }

    public Point scale(int scale) {
        return this.scale(scale, scale);
    }

    public double distanceSquared(Point other) {
        final int dx = this.x - other.x;
        final int dy = this.y - other.y;
        return dx * dx + dy * dy;
    }

    public double distance(Point other) {
        return Math.sqrt(this.distanceSquared(other));
    }

}