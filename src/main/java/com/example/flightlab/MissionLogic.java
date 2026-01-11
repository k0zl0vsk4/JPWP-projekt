package com.example.flightlab;

public class MissionLogic {

    private static final double DESTINATION_X = 5000.0;
    private static final double RUNWAY_LENGTH = 1500.0;
    private static final double HEIGHT = 700.0; //for checking the height

    private MissionType type;
    private boolean lowPassConditionMet = false;

    public MissionLogic(MissionType type) {
        this.type = type;
    }

    public MissionType getType() {
        return type;
    }

    //Setting the plane for take-off depending on the mission
    public void setupPlane(FlightLab_Main.Plane plane) {
        if (type == MissionType.EMERGENCY) {
            //Airborne takeoff, low fuel
            plane.x = 1000;
            plane.y = 100;
            plane.fuel = 30;
            plane.vx = 80;
            plane.throttle = 0.0;
        } else {
            //Normal start
            plane.x = 100;
            plane.y = HEIGHT - 50;
            plane.vx = 0;
            plane.fuel = 100;
            plane.throttle = 0.0;
        }
    }

    //Returning the number of points for victory (or 0 if the mission is ongoing)
    public int checkVictory(FlightLab_Main.Plane plane) {
        // 1. Low pass mission
        if (type == MissionType.LOW_PASS) {
            //Zone above the runway:
            if (plane.x > DESTINATION_X && plane.x < DESTINATION_X + RUNWAY_LENGTH) {
                if (plane.y > HEIGHT - 150 && !plane.onGround) {
                    lowPassConditionMet = true;
                }
                if (plane.onGround) lowPassConditionMet = false;
            }
            if (plane.x > DESTINATION_X + RUNWAY_LENGTH + 100 && lowPassConditionMet) {
                return 80;
            }
        }

        //Remaining mission (landing on the runway)
        else {
            if (plane.onGround && plane.getSpeed() < 5.0) {

                if (type == MissionType.PRECISION) {
                    if (plane.x >= 5200 && plane.x <= 5300) {
                        return 100;
                    }
                }

                else if (plane.x > DESTINATION_X && plane.x < DESTINATION_X + RUNWAY_LENGTH) {
                    if (type == MissionType.EMERGENCY) return 150;
                    return 50;
                }
            }
        }
        return 0;
    }

    public String getObjectiveText() {
        return switch (type) {
            case PRECISION -> "Ląduj w ŻÓŁTEJ strefie (5200-5300m)";
            case EMERGENCY -> "AWARIA! Szybuj do lotniska (5000m)";
            case LOW_PASS -> "Przeleć nisko nad pasem, NIE ląduj!";
            default -> "Ląduj na pasie (5000m)";
        };
    }

    public double getDestX() { return DESTINATION_X; }
    public double getRunwayLen() { return RUNWAY_LENGTH; }
}