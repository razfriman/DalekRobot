/**
 * Created by Raz Friman on 3/2/14.
 */

import rxtxrobot.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class MainForm {
    private JButton connectButton;
    private JPanel panel1;
    private JButton testWaterButton;
    private JNumberTextField turbiditySensorTextField;
    private JNumberTextField salinityMaterialTextField;
    private JNumberTextField turbidityMaterialTextField;
    private JNumberTextField salinitySensorTextField;
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
    private JButton turnRightButton;
    private JButton moveForwardButton;
    private JButton moveBackwardButton;
    private JButton uTurnButton;
    private JNumberTextField movementTicksTextField;
    private JNumberTextField movementInchesTextField;
    private JButton craneUpButton;
    private JButton craneDownButton;
    private JButton bucketCloseButton;
    private JButton bucketOpenButton;
    private JComboBox debugDirectionComboBox;
    private JButton readTurbidityButton;
    private JButton readSalinityButton;
    private JButton readBucketPingButton;
    private JLabel bucketPingLabel;
    private JButton readLightSensorButton;
    private JLabel lightLabel;
    private JButton extendArmButton;
    private JButton retractArmButton;
    private JButton findBridgeButton;
    private JButton crossBridgeButton;
    private JButton soccerBallButton;
    private JComboBox soccerBallComboBox;
    private JButton goToDispensersButton;
    private JButton readServoPingButton;
    private JLabel servoPingLabel;
    private JSlider servoPingSlider;
    private JButton testErrorCorrectionButton;
    private JTextField turnTicksTextField;

    private final JFrame frame;


    public MainForm(JFrame _frame) {

        this.frame = _frame;

        String[] ports = SerialCommunication.checkValidPorts();

        for(String port : ports) {
            portComboBox.addItem(port);
        }

        if(ports.length > 0) {
            portComboBox.setSelectedIndex(0);
        }


        for(int i = 0; i < portComboBox.getItemCount(); i++) {
            if (portComboBox.getItemAt(i).toString().contains("COM")) {
                portComboBox.setSelectedIndex(i);
            } else if (portComboBox.getItemAt(i).toString().contains("tty.usb")) {
                portComboBox.setSelectedIndex(i);
            }
        }

        robotPort = portComboBox.getSelectedItem().toString();

        turbiditySensorTextField.setFormat(JNumberTextField.NUMERIC);
        turbiditySensorTextField.setAllowNegative(false);
        turbiditySensorTextField.setInt(0);

        salinitySensorTextField.setFormat(JNumberTextField.NUMERIC);
        salinitySensorTextField.setAllowNegative(false);
        salinitySensorTextField.setInt(0);

        turbidityMaterialTextField.setFormat(JNumberTextField.NUMERIC);
        salinityMaterialTextField.setAllowNegative(true);
        turbidityMaterialTextField.setInt(0);


        salinityMaterialTextField.setFormat(JNumberTextField.NUMERIC);
        salinityMaterialTextField.setAllowNegative(true);
        salinityMaterialTextField.setInt(0);


        movementTicksTextField.setFormat(JNumberTextField.NUMERIC);
        movementTicksTextField.setAllowNegative(false);
        movementTicksTextField.setInt(100);

        movementInchesTextField.setFormat(JNumberTextField.DECIMAL);
        movementInchesTextField.setAllowNegative(false);
        movementInchesTextField.setDouble(7.0);



        updateRunButtons(false);

        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                try {

                    if (r.isConnected()) {

                        r.close();
                        updateRunButtons(false);

                    } else {

                        r.setPort(robotPort);
                        r.connect();

                        //r.setMotorRampUpTime(0);


                        // Enable the buttons
                        updateRunButtons(true);

                        updateGuiLocation();
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

                Thread t = new Thread() {
                    @Override
                    public void run() {
                        updateRunButtons(false);
                        runAll();
                    }
                };

                t.start();
            }
        });
        testWaterButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                Thread t = new Thread() {
                    @Override
                    public void run() {
                        setup();
                        testWater();

                        goToLocation(FieldDirection.SALINITY_DISPENSER_BOTTOM);
                    }
                };

                t.start();

            }
        });

        testErrorCorrectionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                Thread t = new Thread() {
                    @Override
                    public void run() {
                        setup();

                        goToNextDispenserType(true);
                    }
                };

                t.start();

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


        soccerBallComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                int index = turbidityComboBox.getSelectedIndex();

                if(index == 0) {
                 isCaptureSoccerBall = false;
                } else if (index == 1) {
                     isCaptureSoccerBall = true;
                    soccerBallOnLeft = true;
                } else if (index == 2) {
                    isCaptureSoccerBall = true;
                    soccerBallOnLeft = false;
                }
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


                Thread t = new Thread() {
                    @Override
                    public void run() {
                        setup();

                        lastLocation = FieldDirection.SALINITY_DISPENSER_BOTTOM;

                        activateDispenser();

                        activateDispenser();

                        activateDispenser();
                    }
                };

                t.start();
            }

        });

        deliverMaterialsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                setup();

                lastLocation = FieldDirection.AFTER_CROSS_BRIDGE;

                goToLocation(FieldDirection.DROP_OFF_LOCATION);

                openBucket();


            }
        });
        turbiditySensorTextField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                if (turbiditySensorTextField.getText().isEmpty()) {
                    return;
                }

                turbiditySensorReading = turbiditySensorTextField.getInt();

                calculateRemediationAmounts(salinitySensorReading, turbiditySensorReading);
            }
        });
        salinitySensorTextField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                if (salinitySensorTextField.getText().isEmpty()) {
                    return;
                }

                salinitySensorReading = salinitySensorTextField.getInt();

                calculateRemediationAmounts(salinitySensorReading, turbiditySensorReading);
            }
        });

        turnTicksTextField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                TURN_90_TICKS = Integer.parseInt(turnTicksTextField.getText());
                TURN_180_TICKS = TURN_90_TICKS * 2;
            }
        });

        movementTicksTextField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                if(movementTicksTextField.getText().isEmpty()) {
                    return;
                }

                int movementTicks = movementTicksTextField.getInt();

                debugTicks = movementTicks;
                debugInches = movementTicks * TICK_TO_INCH_CONVERSION;

                movementInchesTextField.setDouble(debugInches);

            }
        });

        movementInchesTextField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                if(movementInchesTextField.getText().isEmpty()) {
                    return;
                }

                double movementInches = movementInchesTextField.getDouble();

                debugInches = movementInches;
                debugTicks = (int) (movementInches * INCH_TO_TICK_CONVERSION);

                movementTicksTextField.setInt(debugTicks);



            }
        });
        moveForwardButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                move(1 * debugDirection, debugTicks, motorSpeed);
            }
        });

        moveBackwardButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                move(-1 * debugDirection, debugTicks, motorSpeed);
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
        debugDirectionComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (debugDirectionComboBox.getSelectedItem().toString().equals("BUCKET")) {
                    debugDirection = BUCKET_FORWARD;
                } else {
                    debugDirection = CRANE_FORWARD;
                }
            }
        });
        readTurbidityButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                readTurbiditySensor();
                calculateRemediationAmounts(salinitySensorReading, turbiditySensorReading);
            }
        });
        readSalinityButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                readSalinitySensor();
                calculateRemediationAmounts(salinitySensorReading, turbiditySensorReading);
            }
        });

        readBucketPingButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                readPingSensorStationary();
            }
        });

        readServoPingButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                readPingDynamic(servoPingSlider.getValue());
            }
        });


        servoPingSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                r.moveServo(PING_SERVO, servoPingSlider.getValue());
            }
        });



        readLightSensorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                readLightSensor();
            }
        });


        extendArmButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                extendArm();
            }
        });


        retractArmButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                retractArm();
            }
        });

        findBridgeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setup();
                lastLocation = FieldDirection.TURBIDITY_DISPENSER_TOP;
                findBridge(true);
            }
        });

        crossBridgeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setup();
                crossBridge();
            }
        });

        soccerBallButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                setup();

                extendArm();

                r.sleep(1000);

                move(BUCKET_FORWARD, 200);

                retractArm();


            }
        });

        goToDispensersButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                setup();

                lastLocation = FieldDirection.START_LOCATION;
                goToLocation(FieldDirection.WATER_WELL);
                goToLocation(FieldDirection.SALINITY_DISPENSER_BOTTOM);
                goToLocation(FieldDirection.SALINITY_DISPENSER_TOP);
                goToLocation(FieldDirection.TURBIDITY_DISPENSER_BOTTOM);
                goToLocation(FieldDirection.TURBIDITY_DISPENSER_TOP);
                goToLocation(FieldDirection.BEFORE_CROSS_BRIDGE_LEFT);

            }
        });

    }

    public void setDebugView(boolean showDebugView) {
        if(showDebugView) {
            frame.setSize(new Dimension(840, 730));
        } else {
            frame.setSize(new Dimension(840, 600));
        }
    }

    public void updateRunButtons(final boolean isEnabled) {


        Runnable runnable = new Runnable()
        {
            @Override
            public void run()
            {

                connectButton.setText(isEnabled ? "Disconnect" : "Connect");

                runButton1.setEnabled(isEnabled);
                testWaterButton.setEnabled(isEnabled);
                dispenseBallsButton.setEnabled(isEnabled);
                deliverMaterialsButton.setEnabled(isEnabled);
                findBridgeButton.setEnabled(isEnabled);
                crossBridgeButton.setEnabled(isEnabled);
                soccerBallButton.setEnabled(isEnabled);

                moveForwardButton.setEnabled(isEnabled);
                moveBackwardButton.setEnabled(isEnabled);
                turnLeftButton.setEnabled(isEnabled);
                turnRightButton.setEnabled(isEnabled);
                uTurnButton.setEnabled(isEnabled);
                craneUpButton.setEnabled(isEnabled);
                craneDownButton.setEnabled(isEnabled);
                bucketOpenButton.setEnabled(isEnabled);
                bucketCloseButton.setEnabled(isEnabled);
                crossBridgeButton.setEnabled(isEnabled);
                readTurbidityButton.setEnabled(isEnabled);
                readSalinityButton.setEnabled(isEnabled);
                readBucketPingButton.setEnabled(isEnabled);
                readServoPingButton.setEnabled(isEnabled);
                servoPingSlider.setEnabled(isEnabled);
                readLightSensorButton.setEnabled(isEnabled);
                extendArmButton.setEnabled(isEnabled);
                retractArmButton.setEnabled(isEnabled);
                goToDispensersButton.setEnabled(isEnabled);
            }
        };

        SwingUtilities.invokeLater(runnable);
    }

    public void updateGuiLocation() {

        Runnable runnable = new Runnable()
        {
            @Override
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
                        case  AFTER_CROSS_BRIDGE:
                            mapLabel.setIcon(new ImageIcon( ImageIO.read(getClass().getResource("images/after bridge right.png"))));
                            break;
                    }
                } catch (Exception e) {
                    // Cannot load icon
                    e.printStackTrace();
                }

            }
        };
        SwingUtilities.invokeLater(runnable);
    }

    public void updateBucketPingLabel() {
        Runnable runnable = new Runnable()
        {
            @Override
            public void run()
            {
                bucketPingLabel.setText(currentBucketPingDistance + " cm");
            }
        };
        SwingUtilities.invokeLater(runnable);
    }

    public void updateServoPingLabel() {
        Runnable runnable = new Runnable()
        {
            @Override
            public void run()
            {
                servoPingLabel.setText(currentServoPingDistance + " cm");
                servoPingSlider.setValue(r.getServoPosition(PING_SERVO));
            }
        };
        SwingUtilities.invokeLater(runnable);
    }

    public void updateLightLabel() {
        Runnable runnable = new Runnable()
        {
            @Override
            public void run()
            {
                lightLabel.setText(Integer.toString(currentLightValue));
            }
        };
        SwingUtilities.invokeLater(runnable);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("MainForm");
        frame.setContentPane(new MainForm(frame).panel1);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();

        frame.setResizable(false);
        frame.setVisible(true);
        frame.setTitle("Dalek Robot");

        frame.setSize(new Dimension(840, 600));
    }

    ////////////////////
    // CONSTANTS
    ////////////////////

    public static final int LINE_SENSOR_ANALOG_PIN = 0; // A0

    public static final int TURBIDITY_PIN = 3; // A3


    public static final int MOTOR_SPEED_FAST = 450;
    public static final int MOTOR_SPEED_MEDIUM = 300;
    public static final int MOTOR_SPEED_SLOW = 200;

    public static final int CRANE_INTERVAL = 10;

    public static final int CRANE_FORWARD = 1;
    public static final int BUCKET_FORWARD = -1;

    public static final int CRANE_DOWN_POSITION = 180;
    public static final int CRANE_UP_POSITION = 90;

    public static final int CARGO_CLOSED_POSITION = 90;
    public static final int CARGO_OPEN_POSITION = 60;

    public static final int CRANE_SERVO = RXTXRobot.SERVO1; // D9
    public static final int CARGO_SERVO = RXTXRobot.SERVO2; // D10
    public static final int PING_SERVO = RXTXRobot.SERVO3; // D11

    public static final int PING_STATIONARY = RXTXRobot.PING2; // D13
    public static final int PING_DYNAMIC = RXTXRobot.PING1; // D8


    public static final int SOCCER_BALL_ARM_MOTOR = RXTXRobot.MOTOR3; // D7

    public static final int SOCCER_BALL_ARM_EXTEND = 1;
    public static final int SOCCER_BALL_ARM_RETRACT = -1;

    public static final double TURN_01_TICKS = 2.0;

    public static int TURN_90_TICKS = 170;
    public static int TURN_180_TICKS = TURN_90_TICKS * 2;

    public static final int QUICK_DELAY = 3;
    public static final int MEDIUM_DELAY = 200;
    public static final int LONG_DELAY = 800;

    public static final double TICK_TO_INCH_CONVERSION = 0.07;
    public static final double INCH_TO_TICK_CONVERSION = 14.28571428571429;

    // Light measurements
    // white = 50
    // grey = 25
    // black = 2,3,4
    public static final int BRIDGE_LIGHT_MARKER_THRESHOLD = 30;


    public static final int DROP_OFF_LOCATION_DISTANCE_THRESHOLD = 35;
    public static final int WATER_WELL_DISTANCE_THRESHOLD = 39;
    public static final int WATER_WELL_PARALLEL_WALL_DISTANCE = 111;
    public static final int CROSS_BRIDGE_DISTANCE_THRESHOLD = 20;
    public static final int DISPENSER_DISTANCE_THRESHOLD = 38;
    public static final int WATER_WELL_REVERSE_DISTANCE_THRESHOLD = 35;
    public static final int CROSS_DISPENSERS_DISTANCE_THRESHOLD = 20;

    public static final int TURBIDITY_MINIMUM= 5;
    public static final int TURBIDITY_MAXIMUM = 750;
    public static final int TURBIDITY_LARGE_AMOUNT = 50;
    public static final int TURBIDITY_SMALL_AMOUNT = 5;

    public static final int SALINITY_MINIMUM = 250;
    public static final int SALINITY_MAXIMUM = 2500;
    public static final int SALINITY_LARGE_AMOUNT = 200;
    public static final int SALINITY_SMALL_AMOUNT = 20;

    public static final int DISPENSER_MATERIAL_LIMIT = 12;

    public static final int PING_SERVO_LEFT = 0;
    public static final int PING_SERVO_MIDDLE = 80;
    public static final int PING_SERVO_RIGHT = 170;


    public static final int LONG_PARALLEL_DISTANCE_FROM_WALL = 121;
    public static final int SHORT_PARALLEL_DISTANCE_FROM_WALL = 62;

    public static final int PUSH_DISPENSER_TICKS = 130;
    public static final int REVERSE_DISPENSER_TICKS = 80;

    ////////////////////
    // VARIABLES
    ////////////////////

    public final RXTXRobot r = new RXTXRobot();
    public FieldDirection lastLocation = FieldDirection.START_LOCATION;

    public FieldDirection salinityLargeLocation = FieldDirection.SALINITY_DISPENSER_TOP;
    public FieldDirection salinitySmallLocation = FieldDirection.SALINITY_DISPENSER_BOTTOM;
    public FieldDirection turbidityLargeLocation = FieldDirection.TURBIDITY_DISPENSER_TOP;
    public FieldDirection turbiditySmallLocation = FieldDirection.TURBIDITY_DISPENSER_BOTTOM;

    public int currentBucketPingDistance = 0;
    public int currentServoPingDistance = 0;
    public int currentLightValue = 0;

    // Between 2,500-12,000
    public int salinityRemediationAmount = 0;
    public int salinityAmountCollected = 0;

    // Between 5-750
    public int turbidityRemediationAmount = 0;
    public int turbidityAmountCollected = 0;

    public int salinitySensorReading = -1;
    public int turbiditySensorReading = -1;

    public int debugDirection = BUCKET_FORWARD;
    public int debugTicks = 100;
    public double debugInches = 7;

    ////////////////////
    // PARAMETERS
    ////////////////////

    public boolean salinityLargeOnTop = false;
    public boolean turbidityLargeOnTop = false;
    public boolean soccerBallOnLeft = false;
    public boolean isCaptureSoccerBall = false;

    public int motorSpeed = MOTOR_SPEED_MEDIUM;
    public boolean debugMode = false;

    public static String robotPort = "/dev/tty.usbmodem1411";

    public void runAll() {
        setup();

        testWater();

        collectRemediationMaterials();

        dropOffMaterials();

        updateRunButtons(true);
    }

    public void setup() {

        lastLocation = FieldDirection.START_LOCATION;

        salinityLargeLocation = salinityLargeOnTop ? FieldDirection.SALINITY_DISPENSER_TOP : FieldDirection.SALINITY_DISPENSER_BOTTOM;
        salinitySmallLocation = salinityLargeOnTop ? FieldDirection.SALINITY_DISPENSER_BOTTOM : FieldDirection.SALINITY_DISPENSER_TOP;

        turbidityLargeLocation = turbidityLargeOnTop ? FieldDirection.TURBIDITY_DISPENSER_TOP : FieldDirection.TURBIDITY_DISPENSER_BOTTOM;
        turbiditySmallLocation = turbidityLargeOnTop ? FieldDirection.TURBIDITY_DISPENSER_BOTTOM : FieldDirection.TURBIDITY_DISPENSER_TOP;

    }




    public void testWater() {

        goToLocation(lastLocation, FieldDirection.WATER_WELL);

        lowerCrane();

        //r.sleep(8000);

        readTurbiditySensor();
        readTurbiditySensor();
        readTurbiditySensor();

        readSalinitySensor();
        readSalinitySensor();

        calculateRemediationAmounts(salinitySensorReading, turbiditySensorReading);

        raiseCrane(true);
    }

    public void readSalinitySensor() {
        salinitySensorReading = r.getConductivity();

        Runnable runnable = new Runnable()
        {
            @Override
            public void run()
            {
                salinitySensorTextField.setText(Integer.toString(salinitySensorReading));

            }
        };
        SwingUtilities.invokeLater(runnable);
    }

    public void readTurbiditySensor() {

        r.refreshAnalogPins();

        turbiditySensorReading = r.getAnalogPin(TURBIDITY_PIN).getValue();

        Runnable runnable = new Runnable()
        {
            @Override
            public void run()
            {
                turbiditySensorTextField.setText(Integer.toString(turbiditySensorReading));

            }
        };
        SwingUtilities.invokeLater(runnable);

    }

    public void calculateRemediationAmounts(int conductivityValue, int turbidityValue) {


        turbidityRemediationAmount = (int) Math.round(-1.7519 * turbidityValue + 990.3);

        salinityRemediationAmount = (int) (37355 * Math.pow(Math.E, -0.008 * conductivityValue));


        turbidityRemediationAmount = Math.min(turbidityRemediationAmount, TURBIDITY_MAXIMUM);
        turbidityRemediationAmount = Math.max(turbidityRemediationAmount, TURBIDITY_MINIMUM);

        salinityRemediationAmount = Math.min(salinityRemediationAmount, SALINITY_MAXIMUM);
        salinityRemediationAmount = Math.max(turbidityRemediationAmount, SALINITY_MINIMUM);


        turbidityAmountCollected = 0;
        salinityAmountCollected = 0;


        Runnable runnable = new Runnable()
        {
            @Override
            public void run()
            {
                salinityMaterialTextField.setText(Integer.toString(salinityRemediationAmount));
                turbidityMaterialTextField.setText(Integer.toString(turbidityRemediationAmount));
            }
        };
        SwingUtilities.invokeLater(runnable);
    }





    public void collectRemediationMaterials() {

        goToLocation(lastLocation, FieldDirection.SALINITY_DISPENSER_BOTTOM);
        collectMaterials(true);

        goToLocation(lastLocation, turbidityLargeLocation);
        collectMaterials(false);
    }

    public void switchDispensers(boolean turnLeftFirst) {


        // Backup a little bit
        move(CRANE_FORWARD, 20);
        //movePastDistance(CRANE_FORWARD, 20, PingDirection.BUCKET);

        if(turnLeftFirst) {
            turnLeft(BUCKET_FORWARD);
        } else {
            turnRight(BUCKET_FORWARD);
        }


        if(turnLeftFirst) {
            readPingSensor(PingDirection.CRANE_LEFT);
        } else {
            readPingSensor(PingDirection.CRANE_RIGHT);
        }

        r.sleep(MEDIUM_DELAY);

        // Move past the current dispenser
        move(BUCKET_FORWARD, 350);


        /* Use ping sensor -- Unreliable
        move(BUCKET_FORWARD, 100);

        r.sleep(MEDIUM_DELAY);

        if(turnLeftFirst) {
            moveUntilDistance(BUCKET_FORWARD, motorSpeed, DISPENSER_PARALLEL_DISTANCE_THRESHOLD, PingDirection.CRANE_LEFT);
        } else {
            moveUntilDistance(BUCKET_FORWARD, motorSpeed, DISPENSER_PARALLEL_DISTANCE_THRESHOLD, PingDirection.CRANE_RIGHT);
        }

        r.sleep(MEDIUM_DELAY);

        move(CRANE_FORWARD, 100);

        r.sleep(MEDIUM_DELAY);
        */


        if(turnLeftFirst) {
            turnRight(BUCKET_FORWARD);
        } else {
            turnLeft(BUCKET_FORWARD);
        }
    }

    public void goToNextDispenserType(boolean isFarSide) {

        /* Old Method - Less sensor detection
        turn180();
        moveUntilDistance(BUCKET_FORWARD, motorSpeed, DISPENSER_DISTANCE_THRESHOLD, PingDirection.BUCKET);
        */


        // New method - Uses parallel ping

        // Backup a little bit
        movePastDistance(CRANE_FORWARD, 20, PingDirection.BUCKET);

        turn180();

        move(BUCKET_FORWARD, 50);

        readPingSensor(PingDirection.BUCKET);
        readPingSensor(PingDirection.CRANE_RIGHT);


        int targetDistance = isFarSide ? LONG_PARALLEL_DISTANCE_FROM_WALL : SHORT_PARALLEL_DISTANCE_FROM_WALL;


        while(currentBucketPingDistance > CROSS_DISPENSERS_DISTANCE_THRESHOLD) {

            r.sleep(50);


            move(BUCKET_FORWARD, 250, MOTOR_SPEED_FAST);


            readPingSensorStationary();

            if(currentBucketPingDistance < CROSS_DISPENSERS_DISTANCE_THRESHOLD) {
                break;
            }

            adjustTurn(PingDirection.CRANE_RIGHT, targetDistance, BUCKET_FORWARD, 10, 20,4,15,true);

            readPingSensorStationary();
        }

    }

    public void collectMaterials(boolean isSalinity) {

        int materialLeft = isSalinity ? salinityRemediationAmount : turbidityRemediationAmount;

        // 5-750 NTU
        //50's and 5's

        // 250-2500 uS
        //20's 200's

        int dispenserLimit = DISPENSER_MATERIAL_LIMIT;

        while(dispenserLimit > 0 && materialLeft >= (isSalinity ? SALINITY_LARGE_AMOUNT - 10 : TURBIDITY_LARGE_AMOUNT - 3)) {
            collectMaterial(isSalinity, true);
            materialLeft -= isSalinity ?  SALINITY_LARGE_AMOUNT : TURBIDITY_LARGE_AMOUNT;

            if(isSalinity) {
                salinityAmountCollected += SALINITY_LARGE_AMOUNT;
            } else {
                turbidityAmountCollected += TURBIDITY_LARGE_AMOUNT;
            }

            System.out.println(String.format("Collecting [%s] - [%d] - [%d/%d]",
                    isSalinity ? "Salinity" : "Turbidity",
                    isSalinity ? SALINITY_LARGE_AMOUNT : TURBIDITY_LARGE_AMOUNT,
                    isSalinity ? salinityAmountCollected : turbidityAmountCollected,
                    isSalinity ? salinityRemediationAmount : turbidityRemediationAmount));

            dispenserLimit--;
        }

        dispenserLimit = DISPENSER_MATERIAL_LIMIT;

        while(dispenserLimit > 0 &&  materialLeft >= (isSalinity? SALINITY_SMALL_AMOUNT - 10 : TURBIDITY_SMALL_AMOUNT - 3)) {
            collectMaterial(isSalinity, false);
            materialLeft -= isSalinity ? SALINITY_SMALL_AMOUNT : TURBIDITY_SMALL_AMOUNT;

            if(isSalinity) {
                salinityAmountCollected += SALINITY_SMALL_AMOUNT;
            } else {
                turbidityAmountCollected += TURBIDITY_SMALL_AMOUNT;
            }

            System.out.println(String.format("Collecting [%s] - [%d] - [%d/%d]",
                    isSalinity ? "Salinity" : "Turbidity",
                    isSalinity ? SALINITY_SMALL_AMOUNT : TURBIDITY_SMALL_AMOUNT,
                    isSalinity ? salinityAmountCollected : turbidityAmountCollected,
                    isSalinity ? salinityRemediationAmount : turbidityRemediationAmount));

            dispenserLimit--;
        }


    }

    public void collectMaterial(boolean isSalinity, boolean isLargeAmount) {

        FieldDirection targetLocation;

        if (isSalinity) {
            targetLocation = isLargeAmount ? salinityLargeLocation : salinitySmallLocation;
        } else {
            targetLocation = isLargeAmount ? turbidityLargeLocation : turbiditySmallLocation;
        }

        goToLocation(lastLocation, targetLocation);

        activateDispenser();


    }

    public void activateDispenser() {

        int pushTicks = PUSH_DISPENSER_TICKS;
        int reverseTicks = REVERSE_DISPENSER_TICKS;

        readPingSensorStationary();

        if(currentBucketPingDistance > 35 && currentBucketPingDistance < 100) {
            System.out.println("Moving closer to dispenser by ping");
            moveUntilDistance(BUCKET_FORWARD, MOTOR_SPEED_SLOW, 35, PingDirection.BUCKET);
            move(CRANE_FORWARD, 40);
        } else if(currentBucketPingDistance >= 100) {
            System.out.println("Warning: Distance very far from dispenser -  [" + currentBucketPingDistance + "]");
        }

        // Push the dispenser
        move(BUCKET_FORWARD, pushTicks, MOTOR_SPEED_FAST);

        r.sleep(MEDIUM_DELAY);

        // Move back a little bit to release the pressure from the dispenser
        move(CRANE_FORWARD, reverseTicks, MOTOR_SPEED_SLOW);

        r.sleep(MEDIUM_DELAY);
    }





    public void dropOffMaterials() {


        goToLocation(lastLocation, FieldDirection.BEFORE_CROSS_BRIDGE_LEFT);

        captureSoccerBall();

        findBridge(!isCaptureSoccerBall || soccerBallOnLeft);

        crossBridge();

        goToLocation(lastLocation, FieldDirection.DROP_OFF_LOCATION);

        openBucket();
    }

    public void captureSoccerBall() {

        if(!isCaptureSoccerBall) {
            turnRight(BUCKET_FORWARD);

        } else {

            if(soccerBallOnLeft) {
                turnLeft(BUCKET_FORWARD);
                moveUntilDistance(BUCKET_FORWARD, MOTOR_SPEED_SLOW,5,PingDirection.BUCKET);
                extendAndRetractArm();
                move(CRANE_FORWARD, 60);
                turnRight(BUCKET_FORWARD);
                turnRight(BUCKET_FORWARD);
            } else {
                turnRight(BUCKET_FORWARD);
                moveUntilDistance(BUCKET_FORWARD, 30, PingDirection.BUCKET);
                extendAndRetractArm();

                turn180();
            }
        }
    }

    public void extendAndRetractArm() {
        extendArm();
        extendArm();
        extendArm();

        r.sleep(500);

        retractArm();
        retractArm();
        retractArm();
    }

    public void findBridge(boolean turnLeftAtEnd) {

        readLightSensor();

        moveAsync(BUCKET_FORWARD, MOTOR_SPEED_SLOW);

        readLightSensor();

        /*
        Old Method - Finds 1 match before stopping motors
        while(currentLightValue < BRIDGE_LIGHT_MARKER_THRESHOLD) {
            readLightSensor();
        }
        */

        int consecutiveHits = 0;

        while(consecutiveHits < 3) {
            if(currentLightValue >= BRIDGE_LIGHT_MARKER_THRESHOLD) {
                consecutiveHits++;
            } else {
                consecutiveHits = 0;
            }

            readLightSensor();
        }


        stopMotors();

        // Face the bridge
        if(turnLeftAtEnd) {
            turnLeft(BUCKET_FORWARD);
        } else {
            turnRight(BUCKET_FORWARD);
        }
    }

    public void crossBridge() {

        goToLocation(FieldDirection.AFTER_CROSS_BRIDGE);

        turnRight(BUCKET_FORWARD);

    }







    public void closeBucket() {
        r.moveServo(CARGO_SERVO, CARGO_CLOSED_POSITION);
    }

    public void openBucket() {
        r.moveServo(CARGO_SERVO, CARGO_OPEN_POSITION);
    }





    public void raiseCrane() {
        raiseCrane(false);
    }

    public void raiseCrane(boolean isSlow) {


        for (int i = r.getServoPosition(CRANE_SERVO); i >= CRANE_UP_POSITION; i -= CRANE_INTERVAL) {
            r.moveServo(CRANE_SERVO, i);
            r.sleep(isSlow ? QUICK_DELAY * 15 : QUICK_DELAY);
        }

        r.sleep(QUICK_DELAY);
    }

    public void lowerCrane() {

        for (int i = r.getServoPosition(CRANE_SERVO); i <= CRANE_DOWN_POSITION; i += CRANE_INTERVAL) {
            r.moveServo(CRANE_SERVO, i);
            r.sleep(QUICK_DELAY);
        }

        r.sleep(QUICK_DELAY);
    }




    public void stopMotors() {
        r.runMotor(RXTXRobot.MOTOR1, 0, RXTXRobot.MOTOR2, 0, 0);
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

        r.sleep(MEDIUM_DELAY);
    }

    public void moveAsync(int direction, int speed) {

        boolean isBucketForward = direction == BUCKET_FORWARD;

        r.runMotor(RXTXRobot.MOTOR1, speed * (isBucketForward ? CRANE_FORWARD : BUCKET_FORWARD),
                RXTXRobot.MOTOR2, speed * (isBucketForward ? CRANE_FORWARD : BUCKET_FORWARD), 0);


    }

    public void moveUntilDistance(int direction, int distanceThreshold, PingDirection servoDirection)
    {
        moveUntilDistance(direction, MOTOR_SPEED_SLOW, distanceThreshold, servoDirection);
    }

    public void moveUntilDistance(int direction, int speed, int distanceThreshold, PingDirection servoDirection)
    {


        readPingSensor(servoDirection);
        r.sleep(MEDIUM_DELAY);

        // Start moving forward
        moveAsync(direction, speed);

        int currentDistance = servoDirection == PingDirection.BUCKET ? currentBucketPingDistance : currentServoPingDistance;

        while(currentDistance > distanceThreshold) {
            readPingSensor(servoDirection);


            currentDistance = servoDirection == PingDirection.BUCKET ? currentBucketPingDistance : currentServoPingDistance;

            System.out.println("Moving until distance: " + currentDistance + "/" + distanceThreshold);

        }

        stopMotors();
    }

    public void movePastDistance(int direction, int distanceThreshold, PingDirection servoDirection)
    {
        movePastDistance(direction, MOTOR_SPEED_SLOW, distanceThreshold, servoDirection);
    }

    public void movePastDistance(int direction, int speed, int distanceThreshold, PingDirection servoDirection)
    {
        // Start moving forward
        moveAsync(direction, speed);

        readPingSensor(servoDirection);

        int currentDistance = servoDirection == PingDirection.BUCKET ? currentBucketPingDistance : currentServoPingDistance;

        while(currentDistance < distanceThreshold) {
            readPingSensor(servoDirection);

            currentDistance = servoDirection == PingDirection.BUCKET ? currentBucketPingDistance : currentServoPingDistance;

            System.out.println("Moving past distance: " + currentDistance + "/" + distanceThreshold);
        }

        stopMotors();
    }





    public void turnLeft(int direction) {

        turnLeft(direction, 90);
    }

    public void turnLeft(int direction, int degrees) {

        degrees = Math.max(degrees, 0);
        degrees = Math.min(degrees, 360);

        boolean isBucketForward = direction == BUCKET_FORWARD;

        int ticks = (int) (TURN_01_TICKS * degrees);


        if(degrees == 90) {
            ticks = TURN_90_TICKS;
        } else if (degrees == 180)
        {
            ticks = TURN_180_TICKS;
        }

        r.runEncodedMotor(
                RXTXRobot.MOTOR1, MOTOR_SPEED_SLOW * (isBucketForward ? CRANE_FORWARD : BUCKET_FORWARD), ticks,
                RXTXRobot.MOTOR2, MOTOR_SPEED_SLOW * (isBucketForward ? BUCKET_FORWARD : CRANE_FORWARD), ticks
        );

        r.sleep(LONG_DELAY);
    }



    public void turnRight(int direction) {

        turnRight(direction, 90);
    }

    public void turnRight(int direction, int degrees) {

        degrees = Math.max(degrees, 0);
        degrees = Math.min(degrees, 360);

        boolean isBucketForward = direction == BUCKET_FORWARD;

        int ticks = (int) (TURN_01_TICKS * degrees);


        if(degrees == 90) {
            ticks = TURN_90_TICKS;
        } else if (degrees == 180)
        {
            ticks = TURN_180_TICKS;
        }

        r.runEncodedMotor(
                RXTXRobot.MOTOR1, MOTOR_SPEED_SLOW * (isBucketForward ? BUCKET_FORWARD : CRANE_FORWARD), ticks,
                RXTXRobot.MOTOR2, MOTOR_SPEED_SLOW * (isBucketForward ? CRANE_FORWARD : BUCKET_FORWARD), ticks
        );

        r.sleep(LONG_DELAY);

    }





    public void turn180() {

        r.runEncodedMotor(
                RXTXRobot.MOTOR1, MOTOR_SPEED_SLOW * CRANE_FORWARD, TURN_180_TICKS,
                RXTXRobot.MOTOR2, MOTOR_SPEED_SLOW * BUCKET_FORWARD, TURN_180_TICKS
        );

        r.sleep(LONG_DELAY);
    }



    public void adjustTurn(PingDirection pingDirection, int targetDistance, int direction, int upperLimit, int upperTurn, int lowerLimit, int lowerTurn) {
        adjustTurn(pingDirection, targetDistance, direction, upperLimit, upperTurn, lowerLimit, lowerTurn, false);

    }

    public void adjustTurn(PingDirection pingDirection, int targetDistance, int direction, int upperLimit, int upperTurn, int lowerLimit, int lowerTurn, boolean isUseMedian) {

        readPingSensor(pingDirection);

        int servoDistance = currentServoPingDistance;

        while(Math.abs(currentServoPingDistance - targetDistance) > upperLimit * 2) {

            r.sleep(200);
            System.out.println("Invalid ping value [" + currentServoPingDistance + "] - Reading again");
            readPingSensor(pingDirection);
        }

        if(isUseMedian) {

            ArrayList<Integer> distances = new ArrayList();
            for(int i = 0; i < 5; i++) {
                readPingSensor(pingDirection);
                distances.add(currentServoPingDistance);
            }
            java.util.Collections.sort(distances);
            servoDistance = distances.get(distances.size() / 2 - 1);
        }


        int diff = servoDistance - targetDistance;

        boolean isAboveLowerLimit = diff > lowerLimit;
        boolean isAboveUpperLimit = diff > upperLimit;

        boolean isBelowLowerLimit = diff < -lowerLimit;
        boolean isBelowUpperLimit = diff < -upperLimit;

        boolean isLeftIfBigger = (direction == CRANE_FORWARD && pingDirection == PingDirection.CRANE_LEFT) ||
                (direction == BUCKET_FORWARD && pingDirection == PingDirection.CRANE_RIGHT);

        if(isAboveLowerLimit) {

            if(isLeftIfBigger) {
                turnLeft(direction, isAboveUpperLimit ? upperTurn : lowerTurn);
            } else {
                turnRight(direction, isAboveUpperLimit ? upperTurn : lowerTurn);
            }

        } else if (isBelowLowerLimit) {

            if(isLeftIfBigger) {
                turnRight(direction, isBelowUpperLimit ? upperTurn : lowerTurn);
            } else {
                turnLeft(direction, isBelowUpperLimit ? upperTurn : lowerTurn);
            }
        }
    }




    public void readPingSensor(PingDirection direction) {

        if(direction == PingDirection.BUCKET) {
            readPingSensorStationary();
        } else {
            readPingDynamic(direction);
        }

        r.sleep(300);
    }

    public void readPingSensorStationary() {

        currentBucketPingDistance = r.getPing(PING_STATIONARY);

        r.sleep(MEDIUM_DELAY);

        updateBucketPingLabel();
    }

    public void readPingDynamic(PingDirection direction) {

        int degrees = PING_SERVO_MIDDLE;

        if(direction == PingDirection.CRANE_MIDDLE) {
            degrees = PING_SERVO_MIDDLE;
        } else if (direction == PingDirection.CRANE_LEFT) {
            degrees = PING_SERVO_LEFT;
        } else if (direction == PingDirection.CRANE_RIGHT) {
            degrees = PING_SERVO_RIGHT;
        }

        readPingDynamic(degrees);

        r.sleep(100);
    }

    public void readPingDynamic(int degrees) {

        if(r.getServoPosition(PING_SERVO) != degrees) {
            r.moveServo(PING_SERVO, degrees);
            r.sleep(800);
        }

        currentServoPingDistance =  r.getPing(PING_DYNAMIC);

        updateServoPingLabel();
    }





    public void readLightSensor() {

        r.refreshAnalogPins();
        currentLightValue = r.getAnalogPin(LINE_SENSOR_ANALOG_PIN).getValue();
        updateLightLabel();
    }






    public void extendArm() {
        r.runMotor(SOCCER_BALL_ARM_MOTOR, SOCCER_BALL_ARM_EXTEND * MOTOR_SPEED_FAST, 500);
        r.sleep(MEDIUM_DELAY);
    }

    public void retractArm() {
        r.runMotor(SOCCER_BALL_ARM_MOTOR, SOCCER_BALL_ARM_RETRACT * MOTOR_SPEED_FAST, 500);
        r.sleep(MEDIUM_DELAY);
    }




    public void goToLocation(FieldDirection toLocation) {
        goToLocation(lastLocation, toLocation);
    }

    public void goToLocation(FieldDirection fromLocation, FieldDirection toLocation) {

        System.out.println("LOCATION: " + toLocation.toString());

        if(fromLocation == toLocation) {
            return;
        }

        if(toLocation == FieldDirection.WATER_WELL) {
            if (fromLocation == FieldDirection.START_LOCATION) {




                moveUntilDistance(CRANE_FORWARD, MOTOR_SPEED_SLOW, WATER_WELL_DISTANCE_THRESHOLD + 40, PingDirection.CRANE_MIDDLE);

                adjustTurn(PingDirection.CRANE_LEFT, WATER_WELL_PARALLEL_WALL_DISTANCE, CRANE_FORWARD, 10,10,3,5);

                moveUntilDistance(CRANE_FORWARD, MOTOR_SPEED_SLOW, WATER_WELL_DISTANCE_THRESHOLD, PingDirection.CRANE_MIDDLE);

                r.sleep(MEDIUM_DELAY);

                lastLocation = toLocation;

            }
        }

        if (toLocation == FieldDirection.SALINITY_DISPENSER_BOTTOM) {
            if (fromLocation == FieldDirection.WATER_WELL) {


                movePastDistance(BUCKET_FORWARD, MOTOR_SPEED_SLOW, WATER_WELL_REVERSE_DISTANCE_THRESHOLD, PingDirection.CRANE_MIDDLE);

                turnRight(BUCKET_FORWARD);

                moveUntilDistance(BUCKET_FORWARD, MOTOR_SPEED_SLOW, DISPENSER_DISTANCE_THRESHOLD + 30, PingDirection.BUCKET);

                adjustTurn(PingDirection.CRANE_LEFT, SHORT_PARALLEL_DISTANCE_FROM_WALL, BUCKET_FORWARD, 10,20,3,10);

                moveUntilDistance(BUCKET_FORWARD, MOTOR_SPEED_SLOW, DISPENSER_DISTANCE_THRESHOLD, PingDirection.BUCKET);

                lastLocation = toLocation;
            } else if (fromLocation == FieldDirection.SALINITY_DISPENSER_TOP) {

                switchDispensers(false);
                lastLocation = toLocation;
            }
        }

        if (toLocation == FieldDirection.SALINITY_DISPENSER_TOP) {
            if (fromLocation == FieldDirection.SALINITY_DISPENSER_BOTTOM) {

                switchDispensers(true);
                lastLocation = toLocation;
            }
        }

        if(toLocation == FieldDirection.TURBIDITY_DISPENSER_BOTTOM) {
            if (fromLocation == FieldDirection.SALINITY_DISPENSER_BOTTOM) {

                goToNextDispenserType(false);

                lastLocation = toLocation;

            } else if (fromLocation == FieldDirection.SALINITY_DISPENSER_TOP) {

                goToNextDispenserType(true);
                switchDispensers(true);

                lastLocation = toLocation;

            } else if (fromLocation == FieldDirection.TURBIDITY_DISPENSER_TOP) {

                switchDispensers(true);

                lastLocation = toLocation;
            }
        }

        if(toLocation == FieldDirection.TURBIDITY_DISPENSER_TOP) {
            if (fromLocation == FieldDirection.SALINITY_DISPENSER_BOTTOM) {


                goToNextDispenserType(false);
                switchDispensers(false);

                lastLocation = toLocation;

            } else if (fromLocation == FieldDirection.SALINITY_DISPENSER_TOP) {

                goToNextDispenserType(true);

                lastLocation = toLocation;

            } else if (fromLocation == FieldDirection.TURBIDITY_DISPENSER_BOTTOM) {

                switchDispensers(false);

                lastLocation = toLocation;
            }
        }


        if(toLocation == FieldDirection.DROP_OFF_LOCATION) {

            if(fromLocation == FieldDirection.AFTER_CROSS_BRIDGE) {

                // Start moving forward
                readPingSensorStationary();

                r.sleep(MEDIUM_DELAY);

                moveUntilDistance(BUCKET_FORWARD, motorSpeed, DROP_OFF_LOCATION_DISTANCE_THRESHOLD, PingDirection.BUCKET);

                lastLocation = toLocation;
            }
        }

        if(toLocation == FieldDirection.BEFORE_CROSS_BRIDGE_LEFT) {

            if (fromLocation == FieldDirection.TURBIDITY_DISPENSER_BOTTOM) {
                goToLocation(FieldDirection.TURBIDITY_DISPENSER_TOP);
                fromLocation = FieldDirection.TURBIDITY_DISPENSER_TOP;
            }

            if (fromLocation == FieldDirection.TURBIDITY_DISPENSER_TOP) {

                turnRight(BUCKET_FORWARD);


                // Move towards the bridge
                move(BUCKET_FORWARD, 260);

                lastLocation = toLocation;
            }

        }

        if(toLocation == FieldDirection.AFTER_CROSS_BRIDGE) {

            // Cross the bridge

            moveUntilDistance(BUCKET_FORWARD, MOTOR_SPEED_SLOW + 100, CROSS_BRIDGE_DISTANCE_THRESHOLD, PingDirection.BUCKET);

            move(CRANE_FORWARD, 100);

        }

        r.sleep(MEDIUM_DELAY);

        updateGuiLocation();
    }


}
