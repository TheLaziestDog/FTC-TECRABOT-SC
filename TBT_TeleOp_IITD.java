package org.firstinspires.ftc.teamcode;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;


/*
 * This file contains an minimal example of a Linear "OpMode". An OpMode is a 'program' that runs in either
 * the autonomous or the teleop period of an FTC match. The names of OpModes appear on the menu
 * of the FTC Driver Station. When a selection is made from the menu, the corresponding OpMode
 * class is instantiated on the Robot Controller and executed.
 *
 * This particular OpMode just executes a basic Tank Drive Teleop for a two wheeled robot
 * It includes all the skeletal structure that all linear OpModes contain.
 *
 * Use Android Studio to Copy this Class, and Paste it into your team's code folder with a new name.
 * Remove or comment out the @Disabled line to add this OpMode to the Driver Station OpMode list
 */

@TeleOp(name="TBT_Teleop_IITD", group="Linear OpMode")
public class TBT_Teleop_IITD extends LinearOpMode {

    private ElapsedTime runtime = new ElapsedTime();

    private DcMotor leftDrive = null;
    private DcMotor rightDrive = null;
    private DcMotor linkage = null;
    private DcMotor linearSlide = null;

    private Servo intakeClaw = null;
    private Servo outtake = null;
    private Servo pivot = null;
    private Servo wrist = null;

    @Override
    public void runOpMode() {
        telemetry.addData("Status", "Initialized");
        telemetry.update();

        leftDrive  = hardwareMap.get(DcMotor.class, "left_drive");
        rightDrive = hardwareMap.get(DcMotor.class, "right_drive");
        linkage  = hardwareMap.get(DcMotor.class, "linkage");
        linearSlide = hardwareMap.get(DcMotor.class, "linear_slide");
        
        //servos
        intakeClaw = hardwareMap.get(Servo.class, "intake_claw");
        outtake = hardwareMap.get(Servo.class, "outtake");
        pivot = hardwareMap.get(Servo.class, "pivot");
        wrist = hardwareMap.get(Servo.class, "wrist");

        double[] setPoses = {0, 0.55, 0, 1, 0.3, 0.65, 1};
        // 0 & 1 Intake Outtake, 2 & 3 Wrist, 4-6 Pivot)

        leftDrive.setDirection(DcMotor.Direction.REVERSE);
        rightDrive.setDirection(DcMotor.Direction.FORWARD);
        
        linkage.setDirection(DcMotor.Direction.FORWARD); // TODO: adjust the orientation
        linkage.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        linkage.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        linkage.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        double dr4bPower = 0;
        double linkageRef = 0; // TODO: Adjust this later
        double linkageError = 0; // TODO: Adjust this later
        
        linearSlide.setDirection(DcMotor.Direction.REVERSE);
        linearSlide.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        double linearSlideRef = 0; // TODO: Adjust this later
        double linearSlideError = 0; // TODO: Adjust this later

        intakeClaw.setPosition(0);
        outtake.setPosition(0.85);
        wrist.setPosition(setPoses[2]);
        pivot.setPosition(setPoses[5]);
        
        // Pivot Stuffs
        int wristPos = 0;
        boolean previousRightBumper = false;
        boolean previousLeftBumper = false;
        double pivotPos = setPoses[5];

        waitForStart();
        runtime.reset();
        
        while (opModeIsActive()) {

            double leftPower;
            double rightPower;

            double drive = gamepad1.left_stick_y;
            double turn  = -gamepad1.right_stick_x;
            
            leftPower    = Range.clip(drive + turn, -1.0, 1.0) ;
            rightPower   = Range.clip(drive - turn, -1.0, 1.0) ;
            
            leftDrive.setPower(leftPower);
            rightDrive.setPower(rightPower);

            // Intake Claw
            if (gamepad2.right_trigger >= 0.5){
                intakeClaw.setPosition(setPoses[1]);
            } else if (gamepad2.left_trigger >= 0.5) {
                intakeClaw.setPosition(0);
            }
            
            // Intake Wrist
            if (gamepad2.right_bumper && !previousRightBumper){
                wristPos++;
            } else if (gamepad2.left_bumper && !previousLeftBumper) {
                wristPos--;
            }
            
            previousRightBumper = gamepad2.right_bumper;
            previousLeftBumper = gamepad2.left_bumper;

            if (wristPos >= 2){
                wristPos = 2;
            } else if (wristPos <= 0){
                wristPos = 0;
            }
            
            switch(wristPos){
                case 0:
                    wrist.setPosition(setPoses[2]);
                    break;
                case 1:
                    wrist.setPosition(0.8);
                    break;
                case 2:
                    wrist.setPosition(setPoses[3]);
                    break;
            }
            
            // Intake Pivot
            pivotPos = (gamepad2.right_stick_x + 1) * (setPoses[6] - setPoses[4]) / 2 + setPoses[4];
            pivot.setPosition(pivotPos);

            // Outtake
            if (gamepad1.right_bumper){
                outtake.setPosition(0.85);
            } else if (gamepad1.left_bumper) {
                outtake.setPosition(0.6);
            }
            
            // Linear Slide
            double linearSlidePower = gamepad2.left_stick_y;
            if (linearSlidePower >= 0.7 && linearSlidePower <= 0.9){
                linearSlidePower = 0.7;
            } else if (linearSlidePower <= 0.7 && linearSlidePower >= 0.9) {
                linearSlidePower = -0.7;
            }
            linearSlide.setPower(linearSlidePower);
            
            // Linkage
            if (gamepad1.right_trigger >= 0.1){
                dr4bPower = gamepad1.right_trigger;
            } else if (gamepad1.left_trigger >= 0.1){
                dr4bPower = -gamepad1.left_trigger;
            } else {
                dr4bPower = 0;
            }
            linkage.setPower(dr4bPower);
            
            // Linkage Encoder
            double linkageCPR = 28 * (5.23 * 3.61 * 2.89);
            int linkageEncoder = linkage.getCurrentPosition();
            double linkageREV = linkageEncoder/linkageCPR;
            double linkageAngle = (linkageREV * 360) % 360;

            telemetry.addData("Linkage Encoder", linkageEncoder);
            telemetry.addData("Linkage Normalized Angle", linkageAngle);
            telemetry.update();
        }
    }
}
