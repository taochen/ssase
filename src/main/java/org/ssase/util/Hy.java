package org.ssase.util;

public class Hy
{
	static final private double zero		= 0.0;
	static final private double one			= 1.0;
	static final private double huge		= 1.0e+300;
	static final private double tiny		= 1.0e-300;
	static final private double two54		= Double.longBitsToDouble(0x4350000000000000L); /* 1.80143985094819840000e+16 */
	static final private double twom54		= Double.longBitsToDouble(0x3c90000000000000L); /* 5.55111512312578270212e-17 */
	static final private double o_threshold	= Double.longBitsToDouble(0x40862e42fefa39efL); /* 7.09782712893383973096e+02 */
	static final private double ln2			= Double.longBitsToDouble(0x3fe62e42fefa39efL); /* 6.93147180559945286227e-01 */
	static final private double ln2_hi		= Double.longBitsToDouble(0x3fe62e42fee00000L); /* 6.93147180369123816490e-01 */
	static final private double ln2_lo		= Double.longBitsToDouble(0x3dea39ef35793c76L); /* 1.90821492927058770002e-10 */
	static final private double invln2		= Double.longBitsToDouble(0x3ff71547652b82feL); /* 1.44269504088896338700e+00 */
	static final private double SINH_XMAX   = Double.longBitsToDouble(0x408633ce8fb9f87dL);

	/* scaled coefficients related to expm1 */
	static final private double Q1  = Double.longBitsToDouble(0xbfa11111111110f4L); /* -3.33333333333331316428e-02 */
	static final private double Q2  = Double.longBitsToDouble(0x3f5a01a019fe5585L); /*  1.58730158725481460165e-03 */
	static final private double Q3  = Double.longBitsToDouble(0xbf14ce199eaadbb7L); /* -7.93650757867487942473e-05 */
	static final private double Q4  = Double.longBitsToDouble(0x3ed0cfca86e65239L); /*  4.00821782732936239552e-06 */
	static final private double Q5  = Double.longBitsToDouble(0xbe8afdb76e09c32dL); /* -2.01099218183624371326e-07 */

	/* coefficents related to log1p */
	static final private double Lp1 = Double.longBitsToDouble(0x3fe5555555555593L);  /* 6.666666666666735130e-01 */
	static final private double Lp2 = Double.longBitsToDouble(0x3fd999999997fa04L);  /* 3.999999999940941908e-01 */
	static final private double Lp3 = Double.longBitsToDouble(0x3fd2492494229359L);  /* 2.857142874366239149e-01 */
	static final private double Lp4 = Double.longBitsToDouble(0x3fcc71c51d8e78afL);  /* 2.222219843214978396e-01 */
	static final private double Lp5 = Double.longBitsToDouble(0x3fc7466496cb03deL);  /* 1.818357216161805012e-01 */
	static final private double Lp6 = Double.longBitsToDouble(0x3fc39a09d078c69fL);  /* 1.531383769920937332e-01 */
	static final private double Lp7 = Double.longBitsToDouble(0x3fc2f112df3e5244L);  /* 1.479819860511658591e-01 */


	/**
	 *	Do not allow instances of this class to be created.
	 */
	private  Hy()
	{
	}


	/**
	 *	Returns a value with the magnitude of x and
	 *	with the sign bit of y.
	 *	If y is NaN then |x| is returned.
	 */
	static public double copysign(double x, double y)
	{
		long longx = Double.doubleToLongBits(x);
		long longy = Double.doubleToLongBits(y);
		
		longx &= 0x7fffffffffffffffL;
		longx |= 0x8000000000000000L & longy;
		return Double.longBitsToDouble(longx);
	}

	/**
	 *	Returns true is longx are the bits of a double NaN.
	 *	This is equivalent to
	 *	Double.isNaN(Double.longBitsToDouble(longx))
	 */
	static private boolean isNaN(long longx)
	{
		return
			((longx & 0x7ff0000000000000L) == 0x7ff0000000000000L)  &&
			((longx & 0x000fffffffffffffL) != 0L);
	}


	/**
	 *	Returns the next machine floating-point number
	 *	next to x in the direction toward y.
	 */
	static public double nextafter(double x, double y)
	{
		long	longx = Double.doubleToLongBits(x);
		long	longy = Double.doubleToLongBits(y);
		
		if (isNaN(longx) || isNaN(longy)) {
			return x + y;
		}
		
		if (x == y) return x;		/* x = y, return x */

		if ((longx & 0x7fffffffffffffffL) == 0L) { /* x = 0 */
			/* return +-minsubnormal */
			x = Double.longBitsToDouble((longy&0x8000000000000000L) | 0x1L);
			y = x*x;
			return ((y == x) ? y : x);		/* raise underflow flag */
		} 

		if (longx >= 0L) {	/* x > 0 */
			if (longx > longy) {
				longx--;		/* x > y, x -= ulp */
			} else {	
				longx++;		/* x < y, x += ulp */
			}
		} else {			/* x < 0 */
			if (longy >= 0L  ||  longx > longy) {  
				longx--;		/* x < y, x -= ulp */
			} else {
				longx++;		/* x > y, x += ulp */
			}
		}

		longy = longx & 0x7ff0000000000000L;
		if (longy >= 0x7ff0000000000000L) {
			/* overflow  */
			return x + x;
		}
		return Double.longBitsToDouble(longx);
	}



	/**
	 * Returns x * 2^n  computed by  exponent  
	 * manipulation rather than by actually performing an 
	 * exponentiation or a multiplication.
	 */
	static public double scalbn(double x, int n)
	{
		long	longx = Double.doubleToLongBits(x);
		int		hx = (int)(longx>>32);
		int		lx = (int)longx;
		int		k = (hx&0x7ff00000)>>20;	/* extract exponent */

		if (k == 0) {				/* 0 or subnormal x */
			if ((longx & 0x7fffffffffffffffL) == 0L)
				return x; /* +-0 */
			x *= two54;
			longx = Double.doubleToLongBits(x);
			hx = (int)(longx >> 32);
			k  = ((hx&0x7ff00000)>>20) - 54; 
			if (n < -50000)
				return tiny*x; 	/*underflow*/
		}
		if (k == 0x7ff)
			return x + x;		/* NaN or Inf */
 
		k += n; 
		if (k > 0x7fe) {
			return huge*copysign(huge,x); /* overflow  */
		} else if (k > 0) {			/* normal result */
			longx &= 0x800fffffffffffffL;
			longx |= ((long)k) << 52;
			return Double.longBitsToDouble(longx);
		} else if (k <= -54) {
			if (n > 50000) {	/* in case integer overflow in n+k */
				return huge*copysign(huge,x);	/*overflow*/
			} else {
				return tiny*copysign(tiny,x); 	/*underflow*/
			}
		} else {
			k += 54;				/* subnormal result */
			longx &= 0x800fffffffffffffL;
			longx |= ((long)k) << 52;
			return twom54 * Double.longBitsToDouble(longx);
		}
	}


	
	/**
	 *	Return the binary exponent of non-zero x.
	 *	ilogb(0) = 0x80000001
	 *	ilogb(inf/NaN) = 0x7fffffff
	 */
	static public int ilogb(double x)
		
	{
		long	longx = Double.doubleToLongBits(x);
		int		hx  = ((int)(longx>>32))&0x7fffffff; /* high word of x */
		
		if (hx < 0x00100000) {
			int lx = (int)longx;
			if ((hx|lx) == 0L) {
				return 0x80000001;	/* ilogb(0) = 0x80000001 */
			} else {
				/* subnormal x */
				int		ix;
				if (hx == 0) {
					for (ix = -1043;  lx > 0;  lx <<= 1) ix--;
				} else {
					for (ix = -1022, hx <<= 11;  hx > 0;  hx <<= 1) ix--;
				}
				return ix;
			}
		} else if (hx < 0x7ff00000) {
			return (hx>>20) - 1023;
		} else {
			return 0x7fffffff;
		}
	}

	/*
	static public double logb(double x)
	{
		long	longx = Double.doubleToLongBits(x);
		int		ix = ((int)(longx>>32))&0x7fffffff;	// high |x|

		if ((0x7fffffffffffffffL & longx) == 0L) {
			return -1.0/Math.abs(x);
		} else if (ix >= 0x7ff00000) {
			return x*x;
		} else {
			return (((ix>>=20)==0) ? -1022.0 : (double)(ix-1023)); 
		}
	}
	*/

	
	
	

	static public double hypot(double x, double y)
	{
		double	a, b;
		double	t1, t2, y1, y2, w;
		int		k;
		
		long	longx = Double.doubleToLongBits(x);
		long	longy = Double.doubleToLongBits(y);

		int		ha = 0x7fffffff & ((int)(longx>>32));
		int		hb = 0x7fffffff & ((int)(longy>>32));

		if (hb > ha) {
			a = y;
			b = x;
			int temp = ha;
			ha = hb;
			hb = temp;
		} else {
			a = x;
			b = y;
		}

		a = Math.abs(a);
		b = Math.abs(b);
		if ((ha-hb) > 0x3c00000) {
			/* x/y > 2**60 */
			return a + b;
		}

		k = 0;
		if (ha > 0x5f300000) {
			/* a > 2**500 */
			if (ha >= 0x7ff00000) {
				/* Inf or NaN */
				w = a + b;			/* for sNaN */
				if (((ha&0xfffff)|__LO(a))==0) w = a;
				if (((hb^0x7ff00000)|__LO(b))==0) w = b;
				return w;
			}
			/* scale a and b by 2**-600 */
			ha -= 0x25800000;
			hb -= 0x25800000;
			k += 600;
			a = setHI(a, ha);
			b = setHI(b, hb);
		}
		if (hb < 0x20b00000) {	/* b < 2**-500 */
			if (hb <= 0x000fffff) {	/* subnormal b or 0 */	
				if ((hb|(__LO(b)))==0) return a;
				/* t1 = 2^1022 */
				t1 = Double.longBitsToDouble(0x7fd0000000000000L);
				b *= t1;
				a *= t1;
				k -= 1022;
			} else {		/* scale a and b by 2^600 */
				ha += 0x25800000; 	/* a *= 2^600 */
				hb += 0x25800000;	/* b *= 2^600 */
				k -= 600;
				a = setHI(a, ha);
				b = setHI(b, hb);
			}
		}
		/* medium size a and b */
		w = a - b;
		if (w > b) {
			t1 = Double.longBitsToDouble((long)ha << 32);
			t2 = a - t1;
			w  = Math.sqrt(t1*t1-(b*(-b)-t2*(a+t1)));
		} else {
			a  = a + a;
			y1 = Double.longBitsToDouble((long)hb << 32);
			y2 = b - y1;
			t1 = Double.longBitsToDouble((long)(ha+0x00100000) << 32);
			t2 = a - t1;
			w  = Math.sqrt(t1*y1-(w*(-w)-(t1*y2+t2*b)));
		}
		if (k != 0) {
			// unscale the result
			int	bits = 0x3ff00000 + (k << 20);
			return w * Double.longBitsToDouble((long)bits << 32);
		} else {
			return w;
		}
	}

	
	static private double setLO(double x, int newLowPart)
	{
		long	lx = Double.doubleToLongBits(x);
		lx &= 0xFFFFFFFF00000000L;
		lx |= newLowPart;
		return Double.longBitsToDouble(lx);
	}


	static private double setHI(double x, int newHiPart)
	{
		long	lx = Double.doubleToLongBits(x);
		lx &= 0x00000000FFFFFFFFL;
		lx |= (((long)newHiPart)<<32);
		return Double.longBitsToDouble(lx);
	}

	static private int __HI(double x)
	{
		return (int)(0xFFFFFFFF&(Double.doubleToLongBits(x)>>32));
	}

	static private int __LO(double x)
	{
		return (int)(0xFFFFFFFF&Double.doubleToLongBits(x));
	}










	/**
	 *	Returns exp(x)-1, the exponential of x minus 1.<br>
	 *	Specifically,<br>
	 *	expm1(å±0) returns å±0<br>
	 *	expm1(+<img src="images/infinity.gif">) returns å±<img src="images/infinity.gif"><br>
	 *	expm1(-<img src="images/infinity.gif">) returns -1.<br>
	 */
	static double expm1(double x)
	{
		double	hi, lo, c=0, t;
		int		k;
		long	longx = Double.doubleToLongBits(x);
		long	xsb = longx & 0x8000000000000000L;
		
		double y = ((xsb == 0) ? x : -x);	/* y = |x| */
		
		/* high word of |x| */
		long hx = longx & 0x7fffffff00000000L;
		
		/* filter out huge and non-finite argument */
		if (hx >= 0x4043687a00000000L) {		/* if |x|>=56*ln2 */
			if (hx >= 0x40862e4200000000L) {	/* if |x|>=709.78... */
				if (hx >= 0x7ff0000000000000L) {
					if ((hx&0x000fffffffffffffL) != 0) {
						return x + x; 	 /* NaN */
					} else {
						return ((xsb == 0)? x : -1.0); /* exp(+-inf)={inf,-1} */
					}
				}
				if (x > o_threshold)
					return huge*huge; /* overflow */
			}
			if (xsb != 0) {
				/* x < -56*ln2, return -1.0 with inexact */
				if (x+tiny < 0.0)		/* raise inexact */
					return tiny-one;	/* return -1 */
			}
		}

		/* argument reduction */
		if (hx > 0x3fd62e4200000000L) {		/* if  |x| > 0.5 ln2 */ 
			if (hx < 0x3ff0a2b200000000L) {	/* and |x| < 1.5 ln2 */
				if (xsb == 0) {
					hi = x - ln2_hi;
					lo = ln2_lo;
					k = 1;
				} else {
					hi = x + ln2_hi;
					lo = -ln2_lo;
					k = -1;
				}
			} else {
				k  = (int)(invln2*x+((xsb==0) ? 0.5 : -0.5));
				t  = k;
				hi = x - t*ln2_hi;	/* t*ln2_hi is exact here */
				lo = t*ln2_lo;
			}
			x  = hi - lo;
			c  = (hi-x)-lo;
		} else if (hx < 0x3c90000000000000L) {  	/* when |x|<2**-54, return x */
			t = huge + x;	/* return x with inexact flags when x!=0 */
			return x - (t-(huge+x));	
		} else {
			k = 0;
		}

		/* x is now in primary range */
		double hfx = 0.5*x;
		double hxs = x*hfx;
		double r1 = one + hxs*(Q1+hxs*(Q2+hxs*(Q3+hxs*(Q4+hxs*Q5))));
		t  = 3.0 - r1*hfx;
		double e  = hxs*((r1-t)/(6.0 - x*t));
		if (k == 0) {
			return x - (x*e-hxs);	/* c is 0 */
		}

		e  = (x*(e-c)-c);
		e -= hxs;
		if (k == -1) {
			return 0.5*(x-e) - 0.5;
		}
		if (k == 1) {
			if (x < -0.25) {
				return -2.0*(e-(x+0.5));
			} else {
				return one + 2.0*(x-e);
			}
		}
		if (k <= -2 || k > 56) {
			/* suffice to return exp(x)-1 */
			y = one - (e-x);
			/* add k to y's exponent */
			long longy = Double.doubleToLongBits(y);
			longy += ((long)k << 52);
			y = Double.longBitsToDouble(longy);
			return y - one;
		}
		t = one;
		if (k < 20) {
			/* t=1-2^-k */
			t = Double.longBitsToDouble(0x3ff0000000000000L-(0x0020000000000000L>>k));
			y = t - (e-x);
			/* add k to y's exponent */
			long longy = Double.doubleToLongBits(y);
			longy += ((long)k << 52);
			y = Double.longBitsToDouble(longy);	
		} else {
			/* 2^-k */
			t = Double.longBitsToDouble(((long)(0x3ff-k))<<52);
			y = x - (e+t);
			y += one;
			/* add k to y's exponent */
			long longy = Double.doubleToLongBits(y);
			longy += ((long)k << 52);
			y = Double.longBitsToDouble(longy);		
		}
		return y;
	}



	/**
	 *	Returns log(x-1), the logarithm of (x minus 1).<br>
	 *	Specifically,<br>
	 *	log1p(å±0) returns å±0<br>
	 *	log1p(-1) returns -<img src="images/infinity.gif"><br>
	 *	log1p(x) returns NaN, if x < -1.<br>
	 *	log1p(å±<img src="images/infinity.gif">) returns å±<img src="images/infinity.gif"><br>
	 */
	static double log1p(double x)
	{
		double	hfsq, f=0, c=0, s, z, R, u;
		int		k, hx, hu=0, ax;
		long	longx = Double.doubleToLongBits(x);
		
		/* high word of x */
		hx = (int)(longx>>32);
		ax = hx&0x7fffffff;
		
		k = 1;
		if (hx < 0x3fda827a) {			/* x < 0.41422  */
			if (ax >= 0x3ff00000) {		/* x <= -1.0 */
				if (x == -1.0) {
					/* log1p(-1) = +inf */
					return -two54/zero;
				} else {
					/* log1p(x<-1) = NaN */
					return (x-x)/(x-x);
				}
			}
			if (ax < 0x3e200000) {		/* |x| < 2**-29 */
				if (two54+x > zero	&&  ax < 0x3c900000) {
					/* raise inexact */
					/* |x| < 2**-54 */
					return x;
				} else {
					return x - x*x*0.5;
				}
			}
			if (hx > 0  ||  hx <= ((int)0xbfd2bec3)) {
				/* -0.2929 < x < 0.41422 */
				k = 0;
				f = x;
				hu = 1;
			}	
		}

		if (hx >= 0x7ff00000)
			return x+x;
	
		if (k != 0) {
			if (hx < 0x43400000) {
				u  = 1.0 + x;
				/* high word of u */
				hu = (int)(Double.doubleToLongBits(u)>>32);
				k  = (hu>>20) - 1023;
				c  = (k > 0 ? 1.0-(u-x) : x-(u-1.0)); /* correction term */
				c /= u;
			} else {
				u  = x;
				/* high word of u */
				hu = (int)(Double.doubleToLongBits(u)>>32);
				k  = (hu>>20) - 1023;
				c  = 0;
			}
			hu &= 0x000fffff;
			if (hu < 0x6a09e) {
				/* normalize u */
				long lu = Double.doubleToLongBits(u);
				lu &= 0x00000000ffffffffL;
				lu |= ((long)hu|0x3ff00000L)<<32;
				u = Double.longBitsToDouble(lu);
			} else {
				k++;
				/* normalize u/2 */
				long lu = Double.doubleToLongBits(u);
				lu &= 0x00000000ffffffffL;
				lu |= ((long)hu|0x3fe00000L)<<32;
				u = Double.longBitsToDouble(lu);
				hu = (0x00100000-hu)>>2;
			}
			f = u - 1.0;
		}

		hfsq = 0.5*f*f;
		if (hu == 0) {	/* |f| < 2**-20 */
			if (f == zero) {
				if (k == 0) {
					return zero;
				} else {
					c += k*ln2_lo;
					return k*ln2_hi+c;
				}
			}
			R = hfsq*(1.0-0.66666666666666666*f);
			if (k == 0) {
				return f - R;
			} else {
				return k*ln2_hi - ((R-(k*ln2_lo+c))-f);
			}
		}
		s = f/(2.0+f); 
		z = s*s;
		R = z*(Lp1+z*(Lp2+z*(Lp3+z*(Lp4+z*(Lp5+z*(Lp6+z*Lp7))))));
		if (k == 0) {
			return f-(hfsq-s*(hfsq+R));
		} else {
			return k*ln2_hi-((hfsq-(s*(hfsq+R)+(k*ln2_lo+c)))-f);
		}
	}



	/**
	 *	Returns the hyperbolic sine of its argument.<br>
	 *	Specifically,<br>
	 *	sinh(å±0) returns å±0<br>
	 *	sinh(å±<img src="images/infinity.gif">) returns å±<img src="images/infinity.gif"><br>
	 *	@param	x	The argument, a double.
	 *	@return	Returns the hyperbolic sine of x.
	 */
	static public double sinh(double x)
	{	
		double	t, w, h;
		int		ix, jx;
		long	longx = Double.doubleToLongBits(x);
		double	absx = Math.abs(x);
		
		/* High word of |x|. */
		jx = (int)(longx>>32);
		ix = jx&0x7fffffff;
		
		/* x is INF or NaN */
		if(ix >= 0x7ff00000) return x+x;	
		
		h = 0.5;
		if (jx < 0) h = -h;
		/* |x| in [0,22], return sign(x)*0.5*(E+E/(E+1))) */
		if (ix < 0x40360000) {		/* |x|<22 */
			if (ix < 0x3e300000) { 	/* |x|<2**-28 */
				/* sinh(tiny) = tiny with inexact */
				if (Double.MAX_VALUE+x > one) {
					return x;
				}
			}
			t = expm1(absx);
			if (ix < 0x3ff00000)
				return h*(2.0*t-t*t/(t+one));
			return h*(t+t/(t+one));
		}
		
		/* |x| in [22, log(maxdouble)] return 0.5*exp(|x|) */
		if (ix < 0x40862E42)
			return h*Math.exp(absx);
		
		/* |x| in [log(maxdouble), overflowthresold] */
		if (absx <= SINH_XMAX) {
			w = Math.exp(0.5*absx);
			t = h*w;
			return t*w;
		}
		
		/* |x| > overflowthresold, sinh(x) overflow */
		return x*Double.MAX_VALUE;
	}


	/**
	 *	Returns the hyperbolic cosine of its argument.<br>
	 *	Specifically,<br>
	 *	cosh(å±0) returns 1.<br>
	 *	cosh(å±<img src="images/infinity.gif">) returns +<img src="images/infinity.gif"><br>
	 *	@param	x	The argument, a double.
	 *	@return	Returns the hyperbolic cosine of x.
	 */
	static public double cosh(double x)
	{	
		double	t, w;
		int		ix;
		long	longx = Double.doubleToLongBits(x);
		double	absx = Math.abs(x);
		double	half = 0.5;
		
		/* High word of |x|. */
		ix = (int)(longx>>32);
		ix = ix&0x7fffffff;
		
		/* x is INF or NaN */
		if(ix >= 0x7ff00000)
			return x*x;	
		
		/* |x| in [0,0.5*ln2], return 1+expm1(|x|)^2/(2*exp(|x|)) */
		if (ix < 0x3fd62e43) {
			t = expm1(absx);
			w = one + t;
			if (ix < 0x3c800000) return w;	/* cosh(tiny) = 1 */
			return one + (t*t)/(w+w);
		}
		
		/* |x| in [0.5*ln2,22], return (exp(|x|)+1/exp(|x|)/2; */
		if (ix < 0x40360000) {
			t = Math.exp(absx);
			return half*t+half/t;
		}
		
		/* |x| in [22, log(maxdouble)] return half*exp(|x|) */
		if (ix < 0x40862E42)
			return half*Math.exp(absx);
		
		/* |x| in [log(maxdouble), overflowthresold] */
		if (absx <= SINH_XMAX) {
			w = Math.exp(half*absx);
			t = half*w;
			return t*w;
		}
		
		/* |x| > overflowthresold, cosh(x) overflow */
		return huge*huge;
	}



	/**
	 *	Returns the hyperbolic tangent of its argument<br>
	 *	Specifically,<br>
	 *	tanh(å±0) returns å±0<br>
	 *	tanh(å±<img src="images/infinity.gif">) returns å±1.<br>
	 *	@param	x	The argument, a double.
	 *	@return	Returns the hyperbolic tangent of x.
	 */
	static public double tanh(double x)
	{
		double	t, z;
		int		jx, ix;
		long	longx = Double.doubleToLongBits(x);
		double	two = 2.0;
		
		/* High word of |x|. */
		jx = (int)(longx>>32);
		ix = jx&0x7fffffff;
		
		/* x is INF or NaN */
		if (ix >= 0x7ff00000) { 
			if (jx >= 0) {
				/* tanh(+-inf)=+-1 */
				return one/x+one;
			} else {
				/* tanh(NaN) = NaN */
				return one/x-one;
			}
		}

		/* |x| < 22 */
		if (ix < 0x40360000) {		/* |x|<22 */
			if (ix < 0x3c800000) {	/* |x|<2**-55 */
				return x*(one+x);    	/* tanh(small) = small */
			}
			if (ix >= 0x3ff00000) {	/* |x|>=1  */
				t = expm1(two*Math.abs(x));
				z = one - two/(t+two);
			} else {
				t = expm1(-two*Math.abs(x));
				z = -t/(t+two);
			}
			/* |x| > 22, return +-1 */
		} else {
			z = one - tiny;		/* raised inexact flag */
		}
		return ((jx >= 0)? z: -z);
	}


	/**
	 *	Returns the inverse hyperbolic sine of its argument.<br>
	 *	Specifically,<br>
	 *	asinh(å±0) returns å±<img src="images/infinity.gif"><br>
	 *	asinh(å±<img src="images/infinity.gif">) returns å±<img src="images/infinity.gif"><br>
	 *	@param	x	The argument, a double.
	 *	@return	Returns the number whose hyperbolic sine is x.
	 */
	static public double asinh(double x)
	{	
		double	t, w;
		int		hx, ix;
		long	longx = Double.doubleToLongBits(x);
		double	absx = Math.abs(x);
		
		hx = (int)(longx>>32);
		ix = hx&0x7fffffff;
		
		/* x is inf or NaN */
		if (ix >= 0x7ff00000) {
			return x + x;
		}

		/* |x|<2**-28 */
		if (ix < 0x3e300000) {
			/* return x inexact except 0 */
			if (huge+x > one) return x;	
		}

		if (ix > 0x41b00000) {
			/* |x| > 2**28 */
			w = Math.log(absx)+ln2;
		} else if (ix > 0x40000000) {
			/* 2**28 > |x| > 2.0 */
			t = absx;
			w = Math.log(2.0*t+one/(Math.sqrt(x*x+one)+t));
		} else {
			/* 2.0 > |x| > 2**-28 */
			t = x*x;
			w = log1p(absx+t/(one+Math.sqrt(one+t)));
		}
		return ((hx > 0) ? w : -w);
	}


	/**
	 *	Returns the inverse hyperbolic cosine of its argument.<br>
	 *	Specifically,<br>
	 *	acosh(1) returns +0<br>
	 *	acosh(å±<img src="images/infinity.gif">) returns +<img src="images/infinity.gif"><br>
	 *	acosh(x) returns NaN, if |x| < 1.<br>
	 *	@param	x	The argument, a double.
	 *	@return	Returns the number whose hyperbolic cosine is x.
	 */
	static public double acosh(double x)
	{	
		double	t;
		long	longx = Double.doubleToLongBits(x);

		int hx = (int)(longx>>32);

		if (hx < 0x3ff00000) {
			/* x < 1 */
			return (x-x)/(x-x);
		} else if (hx >= 0x41b00000) {
			/* x > 2**28 */
			if (hx >= 0x7ff00000) {
				/* x is inf of NaN */
				return x+x;
			} else {
				/* acosh(huge) = log(2x) */
				return Math.log(x) + ln2;
			}
		} else if (longx == 0x3ff0000000000000L) {
			/* acosh(1) = 0 */
			return 0.0;
		} else if (hx > 0x40000000) {
			/* 2**28 > x > 2 */
			t = x*x;
			return Math.log(2.0*x-one/(x+Math.sqrt(t-one)));
		} else {
			/* 1 < x < 2 */
			t = x - one;
			return log1p(t+Math.sqrt(2.0*t+t*t));
		}
	}


	/**
	 *	Returns the inverse hyperbolic tangent of its argument.<br>
	 *	Specifically,<br>
	 *	atanh(å±0) returns å±0<br>
	 *	atanh(å±1) returns +<img src="images/infinity.gif"><br>
	 *	atanh(x) returns NaN, if |x| > 1.<br>
	 *	@param	x	The argument, a double.
	 *	@return	Returns the number whose hyperbolic tangent is x.
	 */
	static public double atanh(double x)
	{
		double	t;
		long	longx = Double.doubleToLongBits(x);
		double	absx = Math.abs(x);

		/* high word */
		int hx = (int)(longx>>32);
		int ix = hx & 0x7fffffff;
		
		if (absx > one) {
			return (x-x)/(x-x);
		}
		
		if (ix == 0x3ff00000) {
			return x / zero;
		}
		
		if (ix < 0x3e300000  &&  (huge+x) > zero) {
			/* x<2**-28 */
			return x;
		}
		
		x = absx;
		if (ix < 0x3fe00000) {		/* x < 0.5 */
			t = x + x;
			t = 0.5*log1p(t+t*x/(one-x));
		} else {
			t = 0.5*log1p((x+x)/(one-x));
		}
		return ((hx >= 0) ? t : -t);
	}

}
