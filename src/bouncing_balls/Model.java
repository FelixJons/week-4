package bouncing_balls;

/**
 * The physics model.
 * <p>
 * This class is where you should implement your bouncing balls model.
 * <p>
 * The code has intentionally been kept as simple as possible, but if you wish, you can improve the design.
 *
 * @author Simon Robillard
 */
class Model {

    double areaWidth, areaHeight;

    Ball[] balls;

    double totalRadius;

    boolean ballsIsFrozen = false;

    Model(double width, double height) {
        areaWidth = width;
        areaHeight = height;

        // Initialize the model with a few balls
        balls = new Ball[2];
        balls[0] = new Ball(width / 3, height * 0.5, 1.0, 0.25, 0.2, 1);
        balls[1] = new Ball(2 * width / 3, height * 0.5, -1.0, -0.5, 0.3, 1);

        totalRadius = balls[0].radius + balls[1].radius;
    }


    void step(double deltaT) {
        // If mass is not assigned here it equals to 0
        balls[0].mass = 1;
        balls[1].mass = 1;
        for (Ball b : balls) {
            // detect collision with the border
            if (circlesIsIntersecting() && !ballsIsFrozen) {
                System.out.println("Intersection occurred!");
                ballsIsFrozen = true;
                // Calculate angle in radians between the x-axis and the line between the balls centres (l)
                double slopeBetweenBalls = Vector.slopeBetweenTwoVectors(balls[0].position, balls[1].position);
                double slopeAxisX = 0;
                double tanBetweenLines = acuteAngleBetweenLines(slopeBetweenBalls, slopeAxisX);
                double radianAngleBetweenLines = Math.atan(tanBetweenLines);

                System.out.println("Radian angle between lines: " + radianAngleBetweenLines);

                // Calculate the clockwise rotation needed to align (l) with the x-axis
                // Observera att degreeAngleBetweenLines är negativ här i hur vi satt upp bollarna, kanske påverkar?
                double rotationAngle = 2 * Math.PI + radianAngleBetweenLines;
                double[][] rotationMatrix = generateRotationMatrixCounterClockwise(rotationAngle);

                System.out.println("Rotation angle for coordinate system: " + rotationAngle);

                // Calculate the projected vectors in the new coordinate system
                Vector ballProjectedVelocity0 = Vector.vectorMatrixTransformation(rotationMatrix, balls[0].velocity);
                Vector ballProjectedVelocity1 = Vector.vectorMatrixTransformation(rotationMatrix, balls[1].velocity);

                System.out.println("Ball0 original initial velocity: " + balls[0].velocity);
                System.out.println("Ball1 original initial velocity: " + balls[1].velocity);

                System.out.println("Ball1 transformed initial velocity: " + ballProjectedVelocity0);
                System.out.println("Ball1 transformed initial velocity: " + ballProjectedVelocity1);

                // Calculate the new velocity across the x-axis
                double initialVelocityX0 = ballProjectedVelocity0.x;
                double initialVelocityX1 = ballProjectedVelocity1.x;
                double newVelocityX0 = calculateNewVelocityX(balls[0].mass, initialVelocityX0,
                        balls[1].mass, initialVelocityX1);
                double newVelocityX1 = calculateNewVelocityX(balls[1].mass, initialVelocityX1,
                        balls[0].mass, initialVelocityX0);

                System.out.println("Ball0 x-velocity after collision: " + newVelocityX0);
                System.out.println("Ball1 x-velocity after collision: " + newVelocityX1);



                double[][] m1 = generateRotationMatrixClockwise(rotationAngle);
                Vector ballFinalVelocity0 = Vector.vectorMatrixTransformation(m1, new Vector(newVelocityX0, ballProjectedVelocity0.y));
                Vector ballFinalVelocity1 = Vector.vectorMatrixTransformation(m1, new Vector(newVelocityX1, ballProjectedVelocity1.y));

                System.out.println("Ball0 velocity vector transformed to original system: " + ballFinalVelocity0);
                System.out.println("Ball1 velocity vector transformed to original system: " + ballFinalVelocity1);

                // Assign the final velocities
                balls[0].velocity = ballFinalVelocity0;
                balls[1].velocity = ballFinalVelocity1;

            }

            if (b.position.x < b.radius || b.position.x > areaWidth - b.radius) {
                b.velocity.x *= -1; // change direction of ball
            }
            if (b.position.y < b.radius || b.position.y > areaHeight - b.radius) {
                b.velocity.y *= -1;
            }

            if (!ballsIsFrozen) {
                moveBalls(deltaT, b);
            }
        }

        // Handle collisions in between circles
    }

    private void moveBalls(double deltaT, Ball b) {
        // compute new position according to the speed of the ball
        b.position.x += deltaT * b.velocity.x;
        b.position.y += deltaT * b.velocity.y;
    }

    double calculateNewVelocityX(double mass1, double v1, double mass2, double v2) {
        return ((mass1 - mass2) / (mass1 + mass2)) * v1
                +
                ((2 * mass2) / (mass1 + mass2)) * v2;
    }

    boolean circlesIsIntersecting() {
        // Pythagoras
        double distanceBetweenCircles = Math.sqrt(Math.pow(balls[0].position.x - balls[1].position.x, 2) + Math.pow(balls[0].position.y - balls[1].position.y, 2));
        return distanceBetweenCircles < totalRadius;
    }

    double acuteAngleBetweenLines(double slope1, double slope2) {
        return (slope1 - slope2) / (1 + (slope1 * slope2));
    }

    double[][] generateRotationMatrixCounterClockwise(double radianAngle) {
        double[][] rotationMatrix = new double[2][2];
        rotationMatrix[0][0] = Math.cos(radianAngle);
        rotationMatrix[0][1] = -Math.sin(radianAngle);
        rotationMatrix[1][0] = Math.sin(radianAngle);
        rotationMatrix[1][1] = Math.cos(radianAngle);
        return rotationMatrix;
    }

    double[][] generateRotationMatrixClockwise(double radianAngle) {
        double[][] rotationMatrix = new double[2][2];
        rotationMatrix[0][0] = Math.cos(radianAngle);
        rotationMatrix[0][1] = Math.sin(radianAngle);
        rotationMatrix[1][0] = -Math.sin(radianAngle);
        rotationMatrix[1][1] = Math.cos(radianAngle);
        return rotationMatrix;
    }


    /**
     * Simple inner class describing balls.
     */
    class Ball {
        Vector position;
        Vector velocity;
        double radius;
        double mass;

        Ball(double x, double y, double vx, double vy, double r, double m) {
            position = new Vector(x, y);
            velocity = new Vector(vx, vy);
            radius = r;
        }
    }

}
