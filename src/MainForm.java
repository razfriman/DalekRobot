import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.text.PlainDocument;

/**
 * Created by raz on 3/2/14.
 */

import rxtxrobot.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.image.BufferedImage;
import java.io.File;





//400 = 27 inches
//300 = 21 inches
//250 = 17.5
//200 = 14 inches
//150 = 11 inches
//100 = 7
// CONVERSION + TICKS * .07 = INCHES


// start to water well = 39 inches
// dispensers = 24 inches apart





public class MainForm {
    private JButton connectButton;
    private JPanel panel1;
    private JButton testWaterButton;
    private JTextField turbiditySensorTextField;
    private JTextField salinityMaterialTextField;
    private JTextField turbidityMaterialTextField;
    private JTextField salinitySensorTextField;
    private JButton runButton1;
    private JComboBox portComboBox;
    private JComboBox turbidityComboBox;
    private JComboBox motorSpeedComboBox;
    private JCheckBox debugModeCheckBox;
    private JComboBox salinityComboBox;
    private JLabel mapLabel;
    private JButton dispenseBallsButton;
    private JButton deliverMaterialsButton;
    private JButton turnLeftButton;
    private JPanel debugPanel;
    private JButton turnRightButton;
    private JButton moveForwardButton;
    private JButton moveBackwardButton;
    private JButton uTurnButton;
    private JTextField movementTicksTextField;
    private JTextField movementInchesTextField;
    private JButton craneUpButton;
    private JButton craneDownButton;
    private JButton bucketCloseButton;
    private JButton bucketOpenButton;
    private JTextField portTextField;

    private final JFrame frame;


    public MainForm(JFrame _frame) {

        this.frame = _frame;

        String[] ports = SerialCommunication.checkValidPorts();

        for(String port : ports) {
            portComboBox.addItem(port);
        }

        portComboBox.setSelectedIndex(0);


        for(int i = 0; i < portComboBox.getItemCount(); i++) {
            if (portComboBox.getItemAt(i).toString().contains("COM")) {
                portComboBox.setSelectedIndex(i);
            } else if (portComboBox.getItemAt(i).toString().contains("tty.usb")) {
                portComboBox.setSelectedIndex(i);
            }
        }

        robotPort = portComboBox.getSelectedItem().toString();

        turbiditySensorTextField.setText("0");
        salinitySensorTextField.setText("0");

        turbidityMaterialTextField.setText("0");
        salinityMaterialTextField.setText("0");

        movementTicksTextField.setText("100");
        movementInchesTextField.setText("7");

        ((PlainDocument) turbiditySensorTextField.getDocument()).setDocumentFilter(new IntegerFilter());
        ((PlainDocument) salinitySensorTextField.getDocument()).setDocumentFilter(new IntegerFilter());
        ((PlainDocument) turbidityMaterialTextField.getDocument()).setDocumentFilter(new IntegerFilter());
        ((PlainDocument) salinityMaterialTextField.getDocument()).setDocumentFilter(new IntegerFilter());
        ((PlainDocument) movementTicksTextField.getDocument()).setDocumentFilter(new IntegerFilter());
        ((PlainDocument) movementInchesTextField.getDocument()).setDocumentFilter(new IntegerFilter());


        updateRunButtons(false);

        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                try {

                    if (r.isConnected()) {

                        r.close();

                        connectButton.setText("Connect");
                        updateRunButtons(false);

                    } else {

                        r.setPort(robotPort);
                        r.connect();

                        // Enable the buttons
                        updateRunButtons(true);

                        updateGuiLocation();

                        connectButton.setText("Disconnect");

                    }
                } catch (Exception ex) {
                    System.out.println("Cannot connect to port");
                    ex.printStackTrace();
                }
            }
        });
        runButton1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                runAll();
            }
        });
        testWaterButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                setup();

                testWater();

                //move(CRANE_FORWARD, 600);
                //turnLeft(BUCKET_FORWARD);
                //move(BUCKET_FORWARD, 300);
            }
        });
        debugModeCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                debugMode = debugModeCheckBox.isSelected();
                r.setVerbose(debugMode);

                setDebugView(debugMode);
            }
        });
        salinityComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                int index = salinityComboBox.getSelectedIndex();

                salinityLargeOnTop = index == 0;
            }
        });
        turbidityComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                int index = turbidityComboBox.getSelectedIndex();

                turbidityLargeOnTop = index == 0;
            }
        });
        motorSpeedComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                int index = motorSpeedComboBox.getSelectedIndex();

                if(index == 0) {
                    motorSpeed = MOTOR_SPEED_SLOW;
                } else if (index == 1) {
                    motorSpeed = MOTOR_SPEED_MEDIUM;
                } else if (index == 2) {
                    motorSpeed = MOTOR_SPEED_FAST;
                }
            }
        });
        portComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                robotPort = portComboBox.getSelectedItem().toString();
            }
        });


        dispenseBallsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                setup();

                lastLocation = SALINITY_DISPENSER_BOTTOM;

                activateDispenser();

                activateDispenser();

                activateDispenser();

            }

        });

        deliverMaterialsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                setup();

                // Stop to insert the balls
                r.sleep(10000);


                int ticks = 600;
                move(BUCKET_FORWARD, ticks);


                // Release the balls
                openBucket();

            }
        });
        turbiditySensorTextField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                try{

                    String input = turbiditySensorTextField.getText();
                    turbiditySensorReading = Integer.parseInt(input);

                    calculateRemediationAmounts(salinitySensorReading, turbiditySensorReading);

                }catch(NumberFormatException ex){
                    // Not a valid number
                }
            }
        });
        salinitySensorTextField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                try {

                    String input = salinitySensorTextField.getText();
                    salinitySensorReading = Integer.parseInt(input);

                    calculateRemediationAmounts(salinitySensorReading, turbiditySensorReading);

                } catch (NumberFormatException ex) {
                    // Not a valid number
                }
            }
        });

        movementTicksTextField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                int movementTicks = Integer.parseInt(movementTicksTextField.getText());

            }
        });

        movementInchesTextField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                int movementInches = Integer.parseInt(movementInchesTextField.getText());

            }
        });
        moveForwardButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                move(-1 * debugDirection, debugTicks, debugSpeed);
            }
        });

        moveBackwardButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                move(-1 * debugDirection, debugTicks, debugSpeed);
            }
        });

        turnLeftButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                turnLeft(debugDirection);
            }
        });
        turnRightButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                turnRight(debugDirection);
            }
        });
        uTurnButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                turn180();
            }
        });
        craneUpButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                raiseCrane();
            }
        });
        craneDownButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                lowerCrane();
            }
        });
        bucketCloseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                closeBucket();
            }
        });
        bucketOpenButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openBucket();
            }
        });
    }

    public void setDebugView(boolean showDebugView) {
        if(showDebugView) {
            frame.setSize(new Dimension(840, 550));
        } else {
            frame.setSize(new Dimension(840, 420));
        }
    }

    public void updateRunButtons(boolean isEnabled) {
        runButton1.setEnabled(isEnabled);
        testWaterButton.setEnabled(isEnabled);
        dispenseBallsButton.setEnabled(isEnabled);
        deliverMaterialsButton.setEnabled(isEnabled);

        moveForwardButton.setEnabled(isEnabled);
        moveBackwardButton.setEnabled(isEnabled);
        turnLeftButton.setEnabled(isEnabled);
        turnRightButton.setEnabled(isEnabled);
        uTurnButton.setEnabled(isEnabled);
        craneUpButton.setEnabled(isEnabled);
        craneDownButton.setEnabled(isEnabled);
        bucketOpenButton.setEnabled(isEnabled);
        bucketCloseButton.setEnabled(isEnabled);

    }

    public void updateGuiLocation() {

        Runnable aRunnable = new Runnable()
        {
            public void run()
            {
                // UI related code

        try {
            switch (lastLocation) {
                case START_LOCATION:
                    mapLabel.setIcon(new ImageIcon( ImageIO.read(getClass().getResource("images/center.png"))));
                    break;
                case WATER_WELL:
                    mapLabel.setIcon(new ImageIcon( ImageIO.read(getClass().getResource("images/water.png"))));
                    break;
                case  SALINITY_DISPENSER_BOTTOM:
                    mapLabel.setIcon(new ImageIcon( ImageIO.read(getClass().getResource("images/salinity bottom.png"))));
                    break;
                case  SALINITY_DISPENSER_TOP:
                    mapLabel.setIcon(new ImageIcon( ImageIO.read(getClass().getResource("images/salinity top.png"))));
                    break;
                case  TURBIDITY_DISPENSER_BOTTOM:
                    mapLabel.setIcon(new ImageIcon( ImageIO.read(getClass().getResource("images/turbidity bottom.png"))));
                    break;
                case  TURBIDITY_DISPENSER_TOP:
                    mapLabel.setIcon(new ImageIcon( ImageIO.read(getClass().getResource("images/turbidity top.png"))));
                    break;
                case  BEFORE_CROSS_BRIDGE_LEFT:
                    mapLabel.setIcon(new ImageIcon( ImageIO.read(getClass().getResource("images/before bridge left.png"))));
                    break;
                case  BEFORE_CROSS_BRIDGE_MIDDLE:
                    mapLabel.setIcon(new ImageIcon( ImageIO.read(getClass().getResource("images/before bridge middle.png"))));
                    break;
                case  BEFORE_CROSS_BRIDGE_RIGHT:
                    mapLabel.setIcon(new ImageIcon( ImageIO.read(getClass().getResource("images/before bridge right.png"))));
                    break;
                case  AFTER_CROSS_BRIDGE_LEFT:
                    mapLabel.setIcon(new ImageIcon( ImageIO.read(getClass().getResource("images/after bridge left.png"))));
                    break;
                case  AFTER_CROSS_BRIDGE_MIDDLE:
                    mapLabel.setIcon(new ImageIcon( ImageIO.read(getClass().getResource("images/after bridge middle.png"))));
                    break;
                case  AFTER_CROSS_BRIDGE_RIGHT:
                    mapLabel.setIcon(new ImageIcon( ImageIO.read(getClass().getResource("images/after bridge right.png"))));
                    break;
            }
        } catch (Exception e) {
            // Cannot load icon
            e.printStackTrace();
        }

            }
        };
        SwingUtilities.invokeLater(aRunnable);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("MainForm");
        frame.setContentPane(new MainForm(frame).panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();

        frame.setResizable(false);
        frame.setVisible(true);
        frame.setTitle("Dalek Robot");

        frame.setSize(new Dimension(840, 420));
    }

    ////////////////////
    // CONSTANTS
    ////////////////////

    public static final int TURBIDITY_PIN = 2;

    public static final int MOTOR_SPEED_FAST = 450;
    public static final int MOTOR_SPEED_MEDIUM = 300;
    public static final int MOTOR_SPEED_SLOW = 250;

    public static final int CRANE_INTERVAL = 10;

    public static final int CRANE_FORWARD = 1;
    public static final int BUCKET_FORWARD = -1;

    public static final int CRANE_REST_POSITION = 90;
    public static final int CRANE_DOWN_POSITION = 0;

    public static final int CARGO_CLOSED_POSITION = 120;
    public static final int CARGO_OPEN_POSITION = 90;

    public static final int BRIDGE_POSITION_UNKNOWN = 0;
    public static final int BRIDGE_POSITION_LEFT = 1;
    public static final int BRIDGE_POSITION_MIDDLE = 2;
    public static final int BRIDGE_POSITION_RIGHT = 3;

    public static final int CRANE_SERVO = RXTXRobot.SERVO1;
    public static final int CARGO_SERVO = RXTXRobot.SERVO2;
    public static final int EXTRA_SERVO = RXTXRobot.SERVO3;

    public static final int START_LOCATION = 1;
    public static final int WATER_WELL = 2;
    public static final int SALINITY_DISPENSER_BOTTOM = 3;
    public static final int SALINITY_DISPENSER_TOP = 4;
    public static final int TURBIDITY_DISPENSER_BOTTOM = 5;
    public static final int TURBIDITY_DISPENSER_TOP = 6;
    public static final int BEFORE_CROSS_BRIDGE_LEFT = 7;
    public static final int BEFORE_CROSS_BRIDGE_MIDDLE = 8;
    public static final int BEFORE_CROSS_BRIDGE_RIGHT = 9;
    public static final int AFTER_CROSS_BRIDGE_LEFT = 10;
    public static final int AFTER_CROSS_BRIDGE_MIDDLE = 11;
    public static final int AFTER_CROSS_BRIDGE_RIGHT = 12;
    public static final int DROP_OFF_LOCATION = 12; // The same as AFTER_CROSS_BRIDGE_RIGHT

    public static final int TURN_90_TICKS = 170;
    public static final int TURN_180_TICKS = 350;

    public static final int QUICK_DELAY = 3;

    public static final double TICK_TO_INCH_CONVERSION = 0.07;
    public static final double INCH_TO_TICK_CONVERSION = 14.28571428571429;

    ////////////////////
    // VARIABLES
    ////////////////////

    public  RXTXRobot r = new RXTXRobot();
    public  int lastLocation = START_LOCATION;

    public  int salinityLargeLocation = SALINITY_DISPENSER_TOP;
    public  int salinitySmallLocation = SALINITY_DISPENSER_BOTTOM;
    public  int turbidityLargeLocation = TURBIDITY_DISPENSER_TOP;
    public  int turbiditySmallLocation = TURBIDITY_DISPENSER_BOTTOM;

    // Between 0-12,000
    public int salinityRemediationAmount = 0;

    // Between 5-750
    public int turbidityRemediationAmount = 0;

    public int salinitySensorReading = -1;
    public int turbiditySensorReading = -1;

    public int bridgePosition = BRIDGE_POSITION_UNKNOWN;

    public int debugDirection = BUCKET_FORWARD;
    public int debugTicks = 100;
    public int debugSpeed = MOTOR_SPEED_MEDIUM;


    ////////////////////
    // PARAMETERS
    ////////////////////

    public boolean salinityLargeOnTop = false;
    public boolean turbidityLargeOnTop = false;
    public int motorSpeed = MOTOR_SPEED_MEDIUM;
    public boolean debugMode = false;

    public static String robotPort = "/dev/tty.usbmodem1411";

    public void runAll() {
        setup();

        testWater();

        collectRemediationMaterials();

        dropOffMaterials();
    }

    public void setup() {

        lastLocation = START_LOCATION;

        salinityLargeLocation = salinityLargeOnTop ? SALINITY_DISPENSER_TOP : SALINITY_DISPENSER_BOTTOM;
        salinitySmallLocation = salinityLargeOnTop ? SALINITY_DISPENSER_BOTTOM : SALINITY_DISPENSER_TOP;

        turbidityLargeLocation = turbidityLargeOnTop ? TURBIDITY_DISPENSER_TOP : TURBIDITY_DISPENSER_BOTTOM;
        turbiditySmallLocation = turbidityLargeOnTop ? TURBIDITY_DISPENSER_BOTTOM : TURBIDITY_DISPENSER_TOP;

        raiseCrane();

        closeBucket();
    }

    public void closeBucket() {
        r.moveServo(CARGO_SERVO, CARGO_CLOSED_POSITION);
    }

    public void openBucket() {
        r.moveServo(CARGO_SERVO, CARGO_OPEN_POSITION);
    }

    public void testWater() {

        goToLocation(lastLocation, WATER_WELL);

        lowerCrane();

        r.sleep(5000);

        r.refreshAnalogPins();
        r.refreshDigitalPins();

        // Get the values from the water sensors
        salinitySensorReading = r.getConductivity();
        turbiditySensorReading = r.getAnalogPin(TURBIDITY_PIN).getValue();

        salinitySensorTextField.setText(Integer.toString(salinitySensorReading));
        turbiditySensorTextField.setText(Integer.toString(turbiditySensorReading));


        // calculate the amount of remediation materials needed
        calculateRemediationAmounts(salinitySensorReading, turbiditySensorReading);

        raiseCrane();
    }

    public void raiseCrane() {

        for (int i = CRANE_REST_POSITION; i >= CRANE_DOWN_POSITION; i -= CRANE_INTERVAL) {
            r.moveServo(CRANE_SERVO, i);
            r.sleep(QUICK_DELAY);
        }

        r.sleep(QUICK_DELAY);
    }

    public void lowerCrane() {

        for (int i = CRANE_DOWN_POSITION; i <= CRANE_REST_POSITION; i += CRANE_INTERVAL) {
            r.moveServo(CRANE_SERVO, i);
            r.sleep(QUICK_DELAY);
        }

        r.sleep(QUICK_DELAY);
    }

    public void calculateRemediationAmounts(int conductivityValue, int turbidityValue) {

        //1.7253x + 1016.1
        turbidityRemediationAmount = (int) Math.round(-1.7253 * turbidityValue + 1016.1);


        //-7.249x+4800.5
        salinityRemediationAmount = (int) Math.round(-7.249 * conductivityValue + 4800.5);

        salinityMaterialTextField.setText(Integer.toString(salinityRemediationAmount));
        turbidityMaterialTextField.setText(Integer.toString(turbidityRemediationAmount));
    }

    public void collectRemediationMaterials() {

        goToLocation(lastLocation, SALINITY_DISPENSER_BOTTOM);
        collectMaterials(true);

        goToLocation(lastLocation, turbidityLargeLocation);
        collectMaterials(false);
    }

    public void goToLocation(int fromLocation, int toLocation) {

        if(fromLocation == toLocation) {
            return;
        }

        if(toLocation == WATER_WELL) {
            if (fromLocation == START_LOCATION) {

                // Move forward to the water
                move(CRANE_FORWARD, 600);

                lastLocation = toLocation;

            }
        }

        if (toLocation == SALINITY_DISPENSER_BOTTOM) {
            if (fromLocation == WATER_WELL) {

                turnLeft(BUCKET_FORWARD);

                //TODO - CALCULATE THE DISTANCE TO THE DISPENSER
                int ticks = 300;
                move(BUCKET_FORWARD, ticks);

                lastLocation = toLocation;
            } else if (fromLocation == SALINITY_DISPENSER_TOP) {

                switchDispensers(false);
                lastLocation = toLocation;
            }
        }

        if (toLocation == SALINITY_DISPENSER_TOP) {
            if (fromLocation == SALINITY_DISPENSER_BOTTOM) {

                switchDispensers(true);
                lastLocation = toLocation;
            }
        }

        if(toLocation == TURBIDITY_DISPENSER_BOTTOM) {
            if (fromLocation == SALINITY_DISPENSER_BOTTOM) {

                goToNextDispenserType();

                lastLocation = toLocation;

            } else if (fromLocation == SALINITY_DISPENSER_TOP) {

                goToNextDispenserType();
                switchDispensers(true);

                lastLocation = toLocation;

            } else if (fromLocation == TURBIDITY_DISPENSER_TOP) {

                switchDispensers(true);

                lastLocation = toLocation;
            }
        }

        if(toLocation == TURBIDITY_DISPENSER_TOP) {
            if (fromLocation == SALINITY_DISPENSER_BOTTOM) {


                goToNextDispenserType();
                switchDispensers(false);

                lastLocation = toLocation;

            } else if (fromLocation == SALINITY_DISPENSER_TOP) {

                goToNextDispenserType();

                lastLocation = toLocation;

            } else if (fromLocation == TURBIDITY_DISPENSER_BOTTOM) {

                switchDispensers(false);

                lastLocation = toLocation;
            }
        }


        if(toLocation == DROP_OFF_LOCATION) {
            if (fromLocation == AFTER_CROSS_BRIDGE_LEFT) {

                turnRight(BUCKET_FORWARD);

                // TODO - CALCULATE DISTANCE TO LOCATION
                int ticks = 1000;
                move(BUCKET_FORWARD, ticks);

                lastLocation = toLocation;
            } else if (fromLocation == AFTER_CROSS_BRIDGE_MIDDLE) {

                turnRight(BUCKET_FORWARD);

                // TODO - CALCULATE DISTANCE TO LOCATION
                int ticksToLocation = 500;
                move(BUCKET_FORWARD, ticksToLocation);

                lastLocation = toLocation;
            }
        }

        updateGuiLocation();
    }

    public void switchDispensers(boolean turnLeftFirst) {

        if(turnLeftFirst) {
            turnLeft(BUCKET_FORWARD);
        } else {
            turnRight(BUCKET_FORWARD);
        }

        // TODO - CALCULATE TICKS
        int ticks = 300;
        move(BUCKET_FORWARD, ticks);

        if(turnLeftFirst) {
            turnRight(BUCKET_FORWARD);
        } else {
            turnLeft(BUCKET_FORWARD);
        }
    }

    public void goToNextDispenserType() {
        turn180();

        // TODO - CALCULATE TICKS
        int ticks = 600;
        move(BUCKET_FORWARD, ticks);
    }

    public void collectMaterials(boolean isSalinity) {

        int materialLeft = isSalinity ? salinityRemediationAmount : turbidityRemediationAmount;

        // 0 - 750 NTU
        while(materialLeft >= (isSalinity ? 1000 : 47)) {
            collectMaterial(isSalinity, true);
            materialLeft -= isSalinity ?  1000 : 50;
        }

        // 0 - 12,000 us
        while(materialLeft >= (isSalinity? 100 : 4)) {
            collectMaterial(isSalinity, false);
            materialLeft -= isSalinity ? 100 : 5;
        }


    }

    public void collectMaterial(boolean isSalinity, boolean isLargeAmount) {

        int targetLocation = 0;

        if (isSalinity) {
            targetLocation = isLargeAmount ? salinityLargeLocation : salinitySmallLocation;
        } else {
            targetLocation = isLargeAmount ? turbidityLargeLocation : turbiditySmallLocation;
        }

        goToLocation(lastLocation, targetLocation);

        activateDispenser();
    }

    public void activateDispenser() {

        int pushTicks = 300;
        int reverseTicks = 100;

        // Push the dispenser
        move(BUCKET_FORWARD, pushTicks, 500);

        r.sleep(500);

        // Move back a little bit to release the pressure from the dispenser
        move(CRANE_FORWARD, reverseTicks);

        r.sleep(500);

    }

    private  int getBridgeRobotLocation(boolean isBeforeCrossBridge) {
        return  getBridgeRobotLocation(bridgePosition, isBeforeCrossBridge);
    }

    private  int getBridgeRobotLocation(int selectedBridgeLocation, boolean isBeforeCrossBridge) {
        switch (selectedBridgeLocation) {
            case BRIDGE_POSITION_LEFT:
                return isBeforeCrossBridge ? BEFORE_CROSS_BRIDGE_LEFT : AFTER_CROSS_BRIDGE_LEFT;
            case BRIDGE_POSITION_MIDDLE:
                return isBeforeCrossBridge ? BEFORE_CROSS_BRIDGE_MIDDLE : AFTER_CROSS_BRIDGE_MIDDLE;
            case BRIDGE_POSITION_RIGHT:
                return isBeforeCrossBridge ? BEFORE_CROSS_BRIDGE_RIGHT : AFTER_CROSS_BRIDGE_RIGHT;
            default:
                return isBeforeCrossBridge ? BEFORE_CROSS_BRIDGE_LEFT : AFTER_CROSS_BRIDGE_LEFT;
        }
    }

    public void crossBridge() {

        goToLocation(lastLocation, BEFORE_CROSS_BRIDGE_LEFT);

        if (bridgePosition == BRIDGE_POSITION_UNKNOWN) {

            // Check the left/middle/right positions for the bridge
            findBridge();
        }

        // Cross the bridge
        goToLocation(lastLocation, getBridgeRobotLocation(false));
    }

    public void findBridge() {
        if (bridgePosition == BRIDGE_POSITION_UNKNOWN) {

            if(checkBridge(BRIDGE_POSITION_LEFT)) {
                bridgePosition = BRIDGE_POSITION_LEFT;
            } else if (checkBridge(BRIDGE_POSITION_MIDDLE)) {
                bridgePosition = BRIDGE_POSITION_MIDDLE;
            } else if (checkBridge(BRIDGE_POSITION_RIGHT)) {
                bridgePosition = BRIDGE_POSITION_RIGHT;
            }
        }
    }

    public boolean checkBridge(int bridgeCheckPosition) {
        if (bridgeCheckPosition != BRIDGE_POSITION_LEFT && bridgeCheckPosition != BRIDGE_POSITION_MIDDLE && bridgeCheckPosition != BRIDGE_POSITION_RIGHT)
        {
            return false;
        }

        // Approach the bridge
        goToLocation(lastLocation, getBridgeRobotLocation(bridgeCheckPosition, true));

        // TODO - GO UP THE RAMP AND USE A SENSOR TO CHECK FOR THE PRESENCE OF THE BRIDGE
        // TODO - GO BACK DOWN AND REPORT WHETHER THE BRIDGE WAS FOUND

        return true;

    }

    public void dropOffMaterials() {

        crossBridge();

        goToLocation(lastLocation, DROP_OFF_LOCATION);

        openBucket();

        // Wait for all of the materials to be released
        r.sleep(5000);

        closeBucket();
    }

    public void stopMotors() {
        r.runEncodedMotor(RXTXRobot.MOTOR1, 0, 0, RXTXRobot.MOTOR2, 0, 0);
    }

    public void move(int direction, int ticks) {
        move(direction, ticks, motorSpeed);
    }

    public void move(int direction, int ticks, int speed) {

        boolean isBucketForward = direction == BUCKET_FORWARD;

        r.runEncodedMotor(
                RXTXRobot.MOTOR1, speed * (isBucketForward ? CRANE_FORWARD : BUCKET_FORWARD), ticks,
                RXTXRobot.MOTOR2, speed * (isBucketForward ? CRANE_FORWARD : BUCKET_FORWARD), ticks
        );

        r.sleep(QUICK_DELAY);
    }

    public void turnLeft(int direction) {

        boolean isBucketForward = direction == BUCKET_FORWARD;

        r.runEncodedMotor(
                RXTXRobot.MOTOR1, motorSpeed * (isBucketForward ? CRANE_FORWARD : BUCKET_FORWARD), TURN_90_TICKS,
                RXTXRobot.MOTOR2, motorSpeed * (isBucketForward ? BUCKET_FORWARD : CRANE_FORWARD), TURN_90_TICKS
        );

        r.sleep(QUICK_DELAY);
    }

    public void turnRight(int direction) {

        boolean isBucketForward = direction == BUCKET_FORWARD;

        r.runEncodedMotor(
                RXTXRobot.MOTOR1, motorSpeed * (isBucketForward ? BUCKET_FORWARD : CRANE_FORWARD), TURN_90_TICKS,
                RXTXRobot.MOTOR2, motorSpeed * (isBucketForward ? CRANE_FORWARD : BUCKET_FORWARD), TURN_90_TICKS
        );

        r.sleep(QUICK_DELAY);
    }

    public void turn180() {

        r.runEncodedMotor(
                RXTXRobot.MOTOR1, MOTOR_SPEED_SLOW * CRANE_FORWARD, TURN_180_TICKS,
                RXTXRobot.MOTOR2, MOTOR_SPEED_SLOW * BUCKET_FORWARD, TURN_180_TICKS
        );

        r.sleep(QUICK_DELAY);
    }
}
