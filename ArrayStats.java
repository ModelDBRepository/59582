/* ArrayStats.java

Methods for generating statistics from arrays of doubles

BPG 22-1-98
*/

import java.lang.Math;


public final class ArrayStats extends Object {

  // Mean of array (int)
  public static double mean(int[] a, int n) {
    // data in array "a"
    // length of array in "n"

    double sum=0;
    int i;

    for (i = 0; i < n; i++)
      sum = sum + (double)a[i];
    return(sum/(double)n);

  }

  // Mean of array (float)
  public static double mean(float[] a, int n) {
    // data in array "a"
    // length of array in "n"

    double sum=0;
    int i;

    for (i = 0; i < n; i++)
      sum = sum + (double)a[i];
    return(sum/(double)n);

  }

  // Mean of array (double)
  public static double mean(double[] a, int n) {
    // data in array "a"
    // length of array in "n"

    double sum=0;
    int i;

    for (i = 0; i < n; i++)
      sum = sum + a[i];
    return(sum/(double)n);

  }


  // Variance of array (unbiased estimate)
  public static double var(int[] a, int n) {
    double sum=0, mn;
    int i;

    if (n < 2)
      return 0;
    mn = mean(a, n);
    for (i = 0; i < n; i++)
      sum = sum + (((double)a[i]-mn)*((double)a[i]-mn));
    return(sum/(double)(n-1));

  }

  // Variance of array (unbiased estimate)
  public static double var(float[] a, int n) {
    double sum=0, mn;
    int i;

    if (n < 2)
      return 0;
    mn = mean(a, n);
    for (i = 0; i < n; i++)
      sum = sum + (((double)a[i]-mn)*((double)a[i]-mn));
    return(sum/(double)(n-1));

  }

  // Variance of array (unbiased estimate)
  public static double var(double[] a, int n) {
    double sum=0, mn;
    int i;

    if (n < 2)
      return 0;
    mn = mean(a, n);
    for (i = 0; i < n; i++)
      sum = sum + ((a[i]-mn)*(a[i]-mn));
    return(sum/(double)(n-1));

  }


  // Standard deviation of array
  public static double std(int[] a, int n) {
    return(Math.sqrt(var(a, n)));
  }

  // Standard deviation of array
  public static double std(float[] a, int n) {
    return(Math.sqrt(var(a, n)));
  }

  // Standard deviation of array
  public static double std(double[] a, int n) {
    return(Math.sqrt(var(a, n)));
  }

}
