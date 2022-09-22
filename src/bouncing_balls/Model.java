package bouncing_balls;

import java.util.Arrays;

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
        // TODO this method implements one step of simulation with a step deltaT
        for (Ball b : balls) {
            // detect collision with the border
            if (circlesIsIntersecting() && !ballsIsFrozen) {
                System.out.println("Intersection occurred!");
                ballsIsFrozen = true;

                double slopeBetweenBalls = Vector.slopeBetweenTwoVectors(balls[0].position, balls[1].position);
                double slopeAxisX = 0;
                double tanBetweenLines = acuteAngleBetweenLines(slopeBetweenBalls, slopeAxisX);
                double radianAngleBetweenLines = Math.atan(tanBetweenLines);
                double degreeAngleBetweenLines = Math.toDegrees(radianAngleBetweenLines);

                // Observera att degreeAngleBetweenLines är negativ här i hur vi satt upp bollarna, kanske påverkar?
                double rotationAngle = 2 * Math.PI + radianAngleBetweenLines;
                /*
                System.out.println(radianAngleBetweenLines);
                System.out.println(rotationAngle);
                System.out.println(Math.toDegrees(rotationAngle));
                 */

                double[][] rotationMatrix = generateRotationMatrix(rotationAngle);
               /*
                System.out.println(Arrays.deepToString(generateRotationMatrix(0)));
                System.out.println(Arrays.deepToString(generateRotationMatrix(Math.PI / 2.0)));
                */

                // Vector vector0 = Vector.vectorMatrixTransformation(generateRotationMatrix(Math.PI / 2.0), new Vector(1, 0));
                // System.out.println(vector0);

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

    boolean circlesIsIntersecting() {
        // Pythagoras
        double distanceBetweenCircles = Math.sqrt(Math.pow(balls[0].position.x - balls[1].position.x, 2) + Math.pow(balls[0].position.y - balls[1].position.y, 2));
        return distanceBetweenCircles < totalRadius;
    }

    double acuteAngleBetweenLines(double slope1, double slope2) {
        return (slope1 - slope2) / (1 + (slope1 * slope2));
    }

    double[][] generateRotationMatrix(double radianAngle) {
        double[][] rotationMatrix = new double[2][2];
        rotationMatrix[0][0] = Math.cos(radianAngle);
        rotationMatrix[0][1] = -Math.sin(radianAngle);
        rotationMatrix[1][0] = Math.sin(radianAngle);
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
