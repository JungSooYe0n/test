package com.trs.netInsight.util.trace;

import javax.servlet.http.HttpServletRequest;

/**
 * Http客户端工具类
 */
public class BrowserUtil {

	private BrowserUtil() {
	}

	/**
	 * 检查当前访问是否来自移动设备
	 */
	public static boolean isMobile(HttpServletRequest request) {
		return detector(request).detectMobileQuick();
	}

	/**
	 * 获取移动设备监测器
	 */
	public static UAgentInfo detector(HttpServletRequest request) {
		String userAgent = request.getHeader("User-Agent");
		String httpAccept = request.getHeader("Accept");
		return new BrowserUtil().new UAgentInfo(userAgent, httpAccept);
	}

	/**
	 * copy from
	 * https://code.google.com/p/mobileesp/source/browse/Java/UAgentInfo.java
	 */
	public class UAgentInfo {
		// User-Agent and Accept HTTP request headers

		private String userAgent = "";
		private String httpAccept = "";

		// Let's store values for quickly accessing the same info multiple
		// times.
		boolean initCompleted = false;
		boolean isWebkit = false; // Stores the result of DetectWebkit()
		boolean isMobilePhone = false; // Stores the result of
		// DetectMobileQuick()
		boolean isIphone = false; // Stores the result of DetectIphone()
		boolean isAndroid = false; // Stores the result of
		// DetectAndroid()
		boolean isAndroidPhone = false; // Stores the result of
		// DetectAndroidPhone()
		boolean isTierTablet = false; // Stores the result of
		// DetectTierTablet()
		boolean isTierIphone = false; // Stores the result of
		// DetectTierIphone()
		boolean isTierRichCss = false; // Stores the result of
		// DetectTierRichCss()
		boolean isTierGenericMobile = false; // Stores the result of
		// DetectTierOtherPhones()

		// Initialize some initial smartphone string variables.
		static final String engineWebKit = "webkit";

		static final String deviceIphone = "iphone";
		static final String deviceIpod = "ipod";
		static final String deviceIpad = "ipad";
		static final String deviceMacPpc = "macintosh"; // Used for
		// disambiguation

		static final String deviceAndroid = "android";
		static final String deviceGoogleTV = "googletv";
		static final String deviceHtcFlyer = "htc_flyer"; // HTC Flyer

		static final String deviceWinPhone7 = "windows phone os 7";
		static final String deviceWinPhone8 = "windows phone 8";
		static final String deviceWinMob = "windows ce";
		static final String deviceWindows = "windows";
		static final String deviceIeMob = "iemobile";
		static final String devicePpc = "ppc"; // Stands for PocketPC
		static final String enginePie = "wm5 pie"; // An old Windows
		// Mobile

		static final String deviceBB = "blackberry";
		static final String deviceBB10 = "bb10"; // For the new BB 10 OS
		static final String vndRIM = "vnd.rim"; // Detectable when BB
		// devices emulate IE or
		// Firefox
		static final String deviceBBStorm = "blackberry95"; // Storm 1
		// and 2
		static final String deviceBBBold = "blackberry97"; // Bold 97x0
		// (non-touch)
		static final String deviceBBBoldTouch = "blackberry 99"; // Bold
		// 99x0
		// (touchscreen)
		static final String deviceBBTour = "blackberry96"; // Tour
		static final String deviceBBCurve = "blackberry89"; // Curve 2
		static final String deviceBBCurveTouch = "blackberry 938"; // Curve
		// Touch
		// 9380
		static final String deviceBBTorch = "blackberry 98"; // Torch
		static final String deviceBBPlaybook = "playbook"; // PlayBook
		// tablet

		static final String deviceSymbian = "symbian";
		static final String deviceS60 = "series60";
		static final String deviceS70 = "series70";
		static final String deviceS80 = "series80";
		static final String deviceS90 = "series90";

		static final String devicePalm = "palm";
		static final String deviceWebOS = "webos"; // For Palm's line of
		// WebOS devices
		static final String deviceWebOShp = "hpwos"; // For HP's line of
		// WebOS devices
		static final String engineBlazer = "blazer"; // Old Palm
		static final String engineXiino = "xiino"; // Another old Palm

		static final String deviceNuvifone = "nuvifone"; // Garmin
		// Nuvifone
		static final String deviceBada = "bada"; // Samsung's Bada OS
		static final String deviceTizen = "tizen"; // Tizen OS
		static final String deviceMeego = "meego"; // Meego OS

		static final String deviceKindle = "kindle"; // Amazon Kindle,
		// eInk one
		static final String engineSilk = "silk-accelerated"; // Amazon's
		// accelerated
		// Silk
		// browser
		// for
		// Kindle
		// Fire

		// Initialize variables for mobile-specific content.
		static final String vndwap = "vnd.wap";
		static final String wml = "wml";

		// Initialize variables for other random devices and mobile browsers.
		static final String deviceTablet = "tablet"; // Generic term for
		// slate and tablet
		// devices
		static final String deviceBrew = "brew";
		static final String deviceDanger = "danger";
		static final String deviceHiptop = "hiptop";
		static final String devicePlaystation = "playstation";
		static final String devicePlaystationVita = "vita";
		static final String deviceNintendoDs = "nitro";
		static final String deviceNintendo = "nintendo";
		static final String deviceWii = "wii";
		static final String deviceXbox = "xbox";
		static final String deviceArchos = "archos";

		static final String engineOpera = "opera"; // Popular browser
		static final String engineNetfront = "netfront"; // Common
		// embedded OS
		// browser
		static final String engineUpBrowser = "up.browser"; // common on
		// some
		// phones
		static final String engineOpenWeb = "openweb"; // Transcoding by
		// OpenWave
		// server
		static final String deviceMidp = "midp"; // a mobile Java
		// technology
		static final String uplink = "up.link";
		static final String engineTelecaQ = "teleca q"; // a modern
		// feature phone
		// browser
		static final String engineObigo = "obigo"; // W 10 is a modern
		// feature phone
		// browser

		static final String devicePda = "pda"; // some devices report
		// themselves as PDAs
		static final String mini = "mini"; // Some mobile browsers put
		// "mini" in their names.
		static final String mobile = "mobile"; // Some mobile browsers
		// put "mobile" in their
		// user agent strings.
		static final String mobi = "mobi"; // Some mobile browsers put
		// "mobi" in their user
		// agent strings.

		// Use Maemo, Tablet, and Linux to test for Nokia"s Internet Tablets.
		static final String maemo = "maemo";
		static final String linux = "linux";
		static final String qtembedded = "qt embedded"; // for Sony Mylo
		static final String mylocom2 = "com2"; // for Sony Mylo also

		// In some UserAgents, the only clue is the manufacturer.
		static final String manuSonyEricsson = "sonyericsson";
		static final String manuericsson = "ericsson";
		static final String manuSamsung1 = "sec-sgh";
		static final String manuSony = "sony";
		static final String manuHtc = "htc";

		// In some UserAgents, the only clue is the operator.
		static final String svcDocomo = "docomo";
		static final String svcKddi = "kddi";
		static final String svcVodafone = "vodafone";

		// Disambiguation strings.
		static final String disUpdate = "update"; // pda vs. update

		/**
		 * Initialize the userAgent and httpAccept variables
		 *
		 * @param userAgent
		 *            the User-Agent header
		 * @param httpAccept
		 *            the Accept header
		 */
		UAgentInfo(String userAgent, String httpAccept) {
			if (userAgent != null) {
				this.userAgent = userAgent.toLowerCase();
			}
			if (httpAccept != null) {
				this.httpAccept = httpAccept.toLowerCase();
			}

			// Intialize key stored values.
			initDeviceScan();
		}

		/**
		 * Return the lower case HTTP_USER_AGENT
		 *
		 * @return userAgent
		 */
		public String getUserAgent() {
			return userAgent;
		}

		/**
		 * Return the lower case HTTP_ACCEPT
		 *
		 * @return httpAccept
		 */
		public String getHttpAccept() {
			return httpAccept;
		}

		/**
		 * Return whether the device is an Iphone or iPod Touch
		 *
		 * @return isIphone
		 */
		public boolean getIsIphone() {
			return isIphone;
		}

		/**
		 * Return whether the device is in the Tablet Tier.
		 *
		 * @return isTierTablet
		 */
		public boolean getIsTierTablet() {
			return isTierTablet;
		}

		/**
		 * Return whether the device is in the Iphone Tier.
		 *
		 * @return isTierIphone
		 */
		public boolean getIsTierIphone() {
			return isTierIphone;
		}

		/**
		 * Return whether the device is in the 'Rich CSS' tier of mobile
		 * devices.
		 *
		 * @return isTierRichCss
		 */
		public boolean getIsTierRichCss() {
			return isTierRichCss;
		}

		/**
		 * Return whether the device is a generic, less-capable mobile device.
		 *
		 * @return isTierGenericMobile
		 */
		public boolean getIsTierGenericMobile() {
			return isTierGenericMobile;
		}

		/**
		 * Initialize Key Stored Values.
		 */
		public void initDeviceScan() {
			// Save these properties to speed processing
			this.isWebkit = detectWebkit();
			this.isIphone = detectIphone();
			this.isAndroid = detectAndroid();
			this.isAndroidPhone = detectAndroidPhone();

			// Generally, these tiers are the most useful for web development
			this.isMobilePhone = detectMobileQuick();
			this.isTierTablet = detectTierTablet();
			this.isTierIphone = detectTierIphone();

			// Optional: Comment these out if you NEVER use them
			this.isTierRichCss = detectTierRichCss();
			this.isTierGenericMobile = detectTierOtherPhones();

			this.initCompleted = true;
		}

		/**
		 * Detects if the current device is an iPhone.
		 *
		 * @return detection of an iPhone
		 */
		public boolean detectIphone() {
			if ((this.initCompleted) || (this.isIphone)) {
				return this.isIphone;
			}

			// The iPad and iPod touch say they're an iPhone! So let's
			// disambiguate.
			return userAgent.contains(deviceIphone) && !detectIpad() && !detectIpod();
		}

		/**
		 * Detects if the current device is an iPod Touch.
		 *
		 * @return detection of an iPod Touch
		 */
		public boolean detectIpod() {
			return userAgent.contains(deviceIpod);
		}

		/**
		 * Detects if the current device is an iPad tablet.
		 *
		 * @return detection of an iPad
		 */
		public boolean detectIpad() {
			return userAgent.contains(deviceIpad) && detectWebkit();
		}

		/**
		 * Detects if the current device is an iPhone or iPod Touch.
		 *
		 * @return detection of an iPhone or iPod Touch
		 */
		public boolean detectIphoneOrIpod() {
			// We repeat the searches here because some iPods may report
			// themselves as an iPhone, which would be okay.
			return userAgent.contains(deviceIphone) || userAgent.contains(deviceIpod);
		}

		/**
		 * Detects *any* iOS device: iPhone, iPod Touch, iPad.
		 *
		 * @return detection of an Apple iOS device
		 */
		public boolean detectIos() {
			return detectIphoneOrIpod() || detectIpad();
		}

		/**
		 * Detects *any* Android OS-based device: phone, tablet, and multi-media
		 * player. Also detects Google TV.
		 *
		 * @return detection of an Android device
		 */
		public boolean detectAndroid() {
			if ((this.initCompleted) || (this.isAndroid)) {
				return this.isAndroid;
			}

			if ((userAgent.contains(deviceAndroid)) || detectGoogleTV()) {
				return true;
			}
			// Special check for the HTC Flyer 7" tablet. It should report here.
			return userAgent.contains(deviceHtcFlyer);
		}

		/**
		 * Detects if the current device is a (small-ish) Android OS-based
		 * device used for calling and/or multi-media (like a Samsung Galaxy
		 * Player). Google says these devices will have 'Android' AND 'mobile'
		 * in user agent. Ignores tablets (Honeycomb and later).
		 *
		 * @return detection of an Android phone
		 */
		public boolean detectAndroidPhone() {
			if ((this.initCompleted) || (this.isAndroidPhone)) {
				return this.isAndroidPhone;
			}

			if (detectAndroid() && (userAgent.contains(mobile))) {
				return true;
			}
			// Special check for Android phones with Opera Mobile. They should
			// report here.
			if (detectOperaAndroidPhone()) {
				return true;
			}
			// Special check for the HTC Flyer 7" tablet. It should report here.
			return userAgent.contains(deviceHtcFlyer);
		}

		/**
		 * Detects if the current device is a (self-reported) Android tablet.
		 * Google says these devices will have 'Android' and NOT 'mobile' in
		 * their user agent.
		 *
		 * @return detection of an Android tablet
		 */
		public boolean detectAndroidTablet() {
			// First, let's make sure we're on an Android device.
			if (!detectAndroid()) {
				return false;
			}

			// Special check for Opera Android Phones. They should NOT report
			// here.
			if (detectOperaMobile()) {
				return false;
			}
			// Special check for the HTC Flyer 7" tablet. It should NOT report
			// here.
			if (userAgent.contains(deviceHtcFlyer)) {
				return false;
			}

			// Otherwise, if it's Android and does NOT have 'mobile' in it,
			// Google says it's a tablet.
			return !userAgent.contains(mobile);
		}

		/**
		 * Detects if the current device is an Android OS-based device and the
		 * browser is based on WebKit.
		 *
		 * @return detection of an Android WebKit browser
		 */
		public boolean detectAndroidWebKit() {
			return detectAndroid() && detectWebkit();
		}

		/**
		 * Detects if the current device is a GoogleTV.
		 *
		 * @return detection of GoogleTV
		 */
		public boolean detectGoogleTV() {
			return userAgent.contains(deviceGoogleTV);
		}

		/**
		 * Detects if the current browser is based on WebKit.
		 *
		 * @return detection of a WebKit browser
		 */
		public boolean detectWebkit() {
			if ((this.initCompleted) || (this.isWebkit)) {
				return this.isWebkit;
			}

			return userAgent.contains(engineWebKit);
		}

		/**
		 * Detects if the current browser is EITHER a Windows Phone 7.x OR 8
		 * device
		 *
		 * @return detection of Windows Phone 7.x OR 8
		 */
		public boolean detectWindowsPhone() {
			return detectWindowsPhone7() || detectWindowsPhone8();
		}

		/**
		 * Detects a Windows Phone 7.x device (in mobile browsing mode).
		 *
		 * @return detection of Windows Phone 7
		 */
		public boolean detectWindowsPhone7() {
			return userAgent.contains(deviceWinPhone7);
		}

		/**
		 * Detects a Windows Phone 8 device (in mobile browsing mode).
		 *
		 * @return detection of Windows Phone 8
		 */
		public boolean detectWindowsPhone8() {
			return userAgent.contains(deviceWinPhone8);
		}

		/**
		 * Detects if the current browser is a Windows Mobile device. Excludes
		 * Windows Phone 7.x and 8 devices. Focuses on Windows Mobile 6.xx and
		 * earlier.
		 *
		 * @return detection of Windows Mobile
		 */
		public boolean detectWindowsMobile() {
			if (detectWindowsPhone()) {
				return false;
			}
			// Most devices use 'Windows CE', but some report 'iemobile'
			// and some older ones report as 'PIE' for Pocket IE.
			// We also look for instances of HTC and Windows for many of their
			// WinMo devices.
			if (userAgent.contains(deviceWinMob) || userAgent.contains(deviceWinMob) || userAgent.contains(deviceIeMob)
					|| userAgent.contains(enginePie)
					|| (userAgent.contains(manuHtc) && userAgent.contains(deviceWindows))
					|| (detectWapWml() && userAgent.contains(deviceWindows))) {
				return true;
			}

			// Test for Windows Mobile PPC but not old Macintosh PowerPC.
			return userAgent.contains(devicePpc) && !(userAgent.contains(deviceMacPpc));

		}

		/**
		 * Detects if the current browser is any BlackBerry. Includes BB10 OS,
		 * but excludes the PlayBook.
		 *
		 * @return detection of Blackberry
		 */
		public boolean detectBlackBerry() {
			return userAgent.contains(deviceBB) || httpAccept.contains(vndRIM) || detectBlackBerry10Phone();

		}

		/**
		 * Detects if the current browser is a BlackBerry 10 OS phone. Excludes
		 * tablets.
		 *
		 * @return detection of a Blackberry 10 device
		 */
		public boolean detectBlackBerry10Phone() {
			return userAgent.contains(deviceBB10) && userAgent.contains(mobile);
		}

		/**
		 * Detects if the current browser is on a BlackBerry tablet device.
		 * Example: PlayBook
		 *
		 * @return detection of a Blackberry Tablet
		 */
		public boolean detectBlackBerryTablet() {
			return userAgent.contains(deviceBBPlaybook);
		}

		/**
		 * Detects if the current browser is a BlackBerry device AND uses a
		 * WebKit-based browser. These are signatures for the new BlackBerry OS
		 * 6. Examples: Torch. Includes the Playbook.
		 *
		 * @return detection of a Blackberry device with WebKit browser
		 */
		public boolean detectBlackBerryWebKit() {
			return detectBlackBerry() && detectWebkit();
		}

		/**
		 * Detects if the current browser is a BlackBerry Touch device, such as
		 * the Storm, Torch, and Bold Touch. Excludes the Playbook.
		 *
		 * @return detection of a Blackberry touchscreen device
		 */
		public boolean detectBlackBerryTouch() {
			return detectBlackBerry() && (userAgent.contains(deviceBBStorm) || userAgent.contains(deviceBBTorch)
					|| userAgent.contains(deviceBBBoldTouch) || userAgent.contains(deviceBBCurveTouch));
		}

		/**
		 * Detects if the current browser is a BlackBerry device AND has a more
		 * capable recent browser. Excludes the Playbook. Examples, Storm, Bold,
		 * Tour, Curve2 Excludes the new BlackBerry OS 6 and 7 browser!!
		 *
		 * @return detection of a Blackberry device with a better browser
		 */
		public boolean detectBlackBerryHigh() {
			// Disambiguate for BlackBerry OS 6 or 7 (WebKit) browser
			return !detectBlackBerryWebKit() && detectBlackBerry()
					&& (detectBlackBerryTouch() || userAgent.contains(deviceBBBold) || userAgent.contains(deviceBBTour)
							|| userAgent.contains(deviceBBCurve));
		}

		/**
		 * Detects if the current browser is a BlackBerry device AND has an
		 * older, less capable browser. Examples: Pearl, 8800, Curve1
		 *
		 * @return detection of a Blackberry device with a poorer browser
		 */
		public boolean detectBlackBerryLow() {
			// Assume that if it's not in the High tier, then it's Low
			return detectBlackBerry() && !(detectBlackBerryHigh() || detectBlackBerryWebKit());
		}

		/**
		 * Detects if the current browser is the Symbian S60 Open Source
		 * Browser.
		 *
		 * @return detection of Symbian S60 Browser
		 */
		public boolean detectS60OssBrowser() {
			// First, test for WebKit, then make sure it's either Symbian or
			// S60.
			return detectWebkit() && (userAgent.contains(deviceSymbian) || userAgent.contains(deviceS60));
		}

		/**
		 *
		 * Detects if the current device is any Symbian OS-based device,
		 * including older S60, Series 70, Series 80, Series 90, and UIQ, or
		 * other browsers running on these devices.
		 *
		 * @return detection of SymbianOS
		 */
		public boolean detectSymbianOS() {
			return userAgent.contains(deviceSymbian) || userAgent.contains(deviceS60) || userAgent.contains(deviceS70)
					|| userAgent.contains(deviceS80) || userAgent.contains(deviceS90);
		}

		/**
		 * Detects if the current browser is on a PalmOS device.
		 *
		 * @return detection of a PalmOS device
		 */
		public boolean detectPalmOS() {
			// Make sure it's not WebOS first
			if (detectPalmWebOS()) {
				return false;
			}

			// Most devices nowadays report as 'Palm', but some older ones
			// reported as Blazer or Xiino.
			return (userAgent.contains(devicePalm)) || (userAgent.contains(engineBlazer))
					|| userAgent.contains(engineXiino);
		}

		/**
		 * Detects if the current browser is on a Palm device running the new
		 * WebOS.
		 *
		 * @return detection of a Palm WebOS device
		 */
		public boolean detectPalmWebOS() {
			return userAgent.contains(deviceWebOS);
		}

		/**
		 * Detects if the current browser is on an HP tablet running WebOS.
		 *
		 * @return detection of an HP WebOS tablet
		 */
		public boolean detectWebOSTablet() {
			return userAgent.contains(deviceWebOShp) && userAgent.contains(deviceTablet);
		}

		/**
		 * Detects Opera Mobile or Opera Mini.
		 *
		 * @return detection of an Opera browser for a mobile device
		 */
		public boolean detectOperaMobile() {
			return userAgent.contains(engineOpera) && (userAgent.contains(mini) || userAgent.contains(mobi));
		}

		/**
		 * Detects Opera Mobile on an Android phone.
		 *
		 * @return detection of an Opera browser on an Android phone
		 */
		public boolean detectOperaAndroidPhone() {
			return userAgent.contains(engineOpera) && (userAgent.contains(deviceAndroid) && userAgent.contains(mobi));
		}

		/**
		 * Detects Opera Mobile on an Android tablet.
		 *
		 * @return detection of an Opera browser on an Android tablet
		 */
		public boolean detectOperaAndroidTablet() {
			return userAgent.contains(engineOpera)
					&& (userAgent.contains(deviceAndroid) && userAgent.contains(deviceTablet));
		}

		/**
		 * Detects if the current device is an Amazon Kindle (eInk devices
		 * only). Note: For the Kindle Fire, use the normal Android methods.
		 *
		 * @return detection of a Kindle
		 */
		public boolean detectKindle() {
			return userAgent.contains(deviceKindle) && !detectAndroid();
		}

		/**
		 * Detects if the current Amazon device is using the Silk Browser. Note:
		 * Typically used by the the Kindle Fire.
		 *
		 * @return detection of an Amazon Kindle Fire in Silk mode.
		 */
		public boolean detectAmazonSilk() {
			return userAgent.contains(engineSilk);
		}

		/**
		 * Detects if the current browser is a Garmin Nuvifone.
		 *
		 * @return detection of a Garmin Nuvifone
		 */
		public boolean detectGarminNuvifone() {
			return userAgent.contains(deviceNuvifone);
		}

		/**
		 * Detects a device running the Bada smartphone OS from Samsung.
		 *
		 * @return detection of a Bada device
		 */
		public boolean detectBada() {
			return userAgent.contains(deviceBada);
		}

		/**
		 * Detects a device running the Tizen smartphone OS.
		 *
		 * @return detection of a Tizen device
		 */
		public boolean detectTizen() {
			return userAgent.contains(deviceTizen);
		}

		/**
		 * Detects a device running the Meego OS.
		 *
		 * @return detection of a Meego device
		 */
		public boolean detectMeego() {
			return userAgent.contains(deviceMeego);
		}

		/**
		 * Detects the Danger Hiptop device.
		 *
		 * @return detection of a Danger Hiptop
		 */
		public boolean detectDangerHiptop() {
			return userAgent.contains(deviceDanger) || userAgent.contains(deviceHiptop);
		}

		/**
		 * Detects if the current browser is a Sony Mylo device.
		 *
		 * @return detection of a Sony Mylo device
		 */
		public boolean detectSonyMylo() {
			return userAgent.contains(manuSony) && (userAgent.contains(qtembedded) || userAgent.contains(mylocom2));
		}

		/**
		 * Detects if the current device is on one of the Maemo-based Nokia
		 * Internet Tablets.
		 *
		 * @return detection of a Maemo OS tablet
		 */
		public boolean detectMaemoTablet() {
			if (userAgent.contains(maemo)) {
				return true;
			} else if (userAgent.contains(linux) && userAgent.contains(deviceTablet) && !detectWebOSTablet()
					&& !detectAndroid()) {
				return true;
			}
			return false;
		}

		/**
		 * Detects if the current device is an Archos media player/Internet
		 * tablet.
		 *
		 * @return detection of an Archos media player
		 */
		public boolean detectArchos() {
			return userAgent.contains(deviceArchos);
		}

		/**
		 * Detects if the current device is an Internet-capable game console.
		 * Includes many handheld consoles.
		 *
		 * @return detection of any Game Console
		 */
		public boolean detectGameConsole() {
			return detectSonyPlaystation() || detectNintendo() || detectXbox();
		}

		/**
		 * Detects if the current device is a Sony Playstation.
		 *
		 * @return detection of Sony Playstation
		 */
		public boolean detectSonyPlaystation() {
			return userAgent.contains(devicePlaystation);
		}

		/**
		 * Detects if the current device is a handheld gaming device with a
		 * touchscreen and modern iPhone-class browser. Includes the Playstation
		 * Vita.
		 *
		 * @return detection of a handheld gaming device
		 */
		public boolean detectGamingHandheld() {
			return (userAgent.contains(devicePlaystation)) && (userAgent.contains(devicePlaystationVita));
		}

		/**
		 * Detects if the current device is a Nintendo game device.
		 *
		 * @return detection of Nintendo
		 */
		public boolean detectNintendo() {
			return userAgent.contains(deviceNintendo) || userAgent.contains(deviceWii)
					|| userAgent.contains(deviceNintendoDs);
		}

		/**
		 * Detects if the current device is a Microsoft Xbox.
		 *
		 * @return detection of Xbox
		 */
		public boolean detectXbox() {
			return userAgent.contains(deviceXbox);
		}

		/**
		 * Detects whether the device is a Brew-powered device.
		 *
		 * @return detection of a Brew device
		 */
		public boolean detectBrewDevice() {
			return userAgent.contains(deviceBrew);
		}

		/**
		 * Detects whether the device supports WAP or WML.
		 *
		 * @return detection of a WAP- or WML-capable device
		 */
		public boolean detectWapWml() {
			return httpAccept.contains(vndwap) || httpAccept.contains(wml);
		}

		/**
		 * Detects if the current device supports MIDP, a mobile Java
		 * technology.
		 *
		 * @return detection of a MIDP mobile Java-capable device
		 */
		public boolean detectMidpCapable() {
			return userAgent.contains(deviceMidp) || httpAccept.contains(deviceMidp);
		}

		// *****************************
		// Device Classes
		// *****************************

		/**
		 * Check to see whether the device is any device in the 'smartphone'
		 * category.
		 *
		 * @return detection of a general smartphone device
		 */
		public boolean detectSmartphone() {
			// Exclude duplicates from TierIphone
			return (detectTierIphone() || detectS60OssBrowser() || detectSymbianOS() || detectWindowsMobile()
					|| detectBlackBerry() || detectPalmOS());
		}

		/**
		 * Detects if the current device is a mobile device. This method catches
		 * most of the popular modern devices. Excludes Apple iPads and other
		 * modern tablets.
		 *
		 * @return detection of any mobile device using the quicker method
		 */
		public boolean detectMobileQuick() {
			// Let's exclude tablets
			if (detectTierTablet()) {
				return false;
			}

			if (initCompleted || isMobilePhone) {
				return isMobilePhone;
			}

			// Most mobile browsing is done on smartphones
			if (detectSmartphone()) {
				return true;
			}

			if (detectWapWml() || detectBrewDevice() || detectOperaMobile()) {
				return true;
			}

			if ((userAgent.contains(engineObigo)) || (userAgent.contains(engineNetfront))
					|| (userAgent.contains(engineUpBrowser)) || (userAgent.contains(engineOpenWeb))) {
				return true;
			}

			if (detectDangerHiptop() || detectMidpCapable() || detectMaemoTablet() || detectArchos()) {
				return true;
			}

			if ((userAgent.contains(devicePda)) && (!userAgent.contains(disUpdate))) { // no
																						// index
																						// found
				return true;
			}

			if (userAgent.contains(mobile)) {
				return true;
			}

			// We also look for Kindle devices
			return detectKindle() || detectAmazonSilk();

		}

		/**
		 * The longer and more thorough way to detect for a mobile device. Will
		 * probably detect most feature phones, smartphone-class devices,
		 * Internet Tablets, Internet-enabled game consoles, etc. This ought to
		 * catch a lot of the more obscure and older devices, also -- but no
		 * promises on thoroughness!
		 *
		 * @return detection of any mobile device using the more thorough method
		 */
		public boolean detectMobileLong() {
			if (detectMobileQuick() || detectGameConsole() || detectSonyMylo()) {
				return true;
			}

			// detect older phones from certain manufacturers and operators.
			return userAgent.contains(uplink) || userAgent.contains(manuSonyEricsson)
					|| userAgent.contains(manuericsson) || userAgent.contains(manuSamsung1)
					|| userAgent.contains(svcDocomo) || userAgent.contains(svcKddi) || userAgent.contains(svcVodafone);

		}

		// *****************************
		// For Mobile Web Site Design
		// *****************************

		/**
		 * The quick way to detect for a tier of devices. This method detects
		 * for the new generation of HTML 5 capable, larger screen tablets.
		 * Includes iPad, Android (e.g., Xoom), BB Playbook, WebOS, etc.
		 *
		 * @return detection of any device in the Tablet Tier
		 */
		public boolean detectTierTablet() {
			if (this.initCompleted || this.isTierTablet) {
				return this.isTierTablet;
			}

			return detectIpad() || detectAndroidTablet() || detectBlackBerryTablet() || detectWebOSTablet();
		}

		/**
		 * The quick way to detect for a tier of devices. This method detects
		 * for devices which can display iPhone-optimized web content. Includes
		 * iPhone, iPod Touch, Android, Windows Phone 7 and 8, BB10, WebOS,
		 * Playstation Vita, etc.
		 *
		 * @return detection of any device in the iPhone/Android/Windows
		 *         Phone/BlackBerry/WebOS Tier
		 */
		public boolean detectTierIphone() {
			if (this.initCompleted || this.isTierIphone) {
				return this.isTierIphone;
			}

			return detectIphoneOrIpod() || detectAndroidPhone() || detectWindowsPhone() || detectBlackBerry10Phone()
					|| (detectBlackBerryWebKit() && detectBlackBerryTouch()) || detectPalmWebOS() || detectBada()
					|| detectTizen() || detectGamingHandheld();
		}

		/**
		 * The quick way to detect for a tier of devices. This method detects
		 * for devices which are likely to be capable of viewing CSS content
		 * optimized for the iPhone, but may not necessarily support JavaScript.
		 * Excludes all iPhone Tier devices.
		 *
		 * @return detection of any device in the 'Rich CSS' Tier
		 */
		public boolean detectTierRichCss() {
			if (this.initCompleted || this.isTierRichCss) {
				return this.isTierRichCss;
			}

			boolean result = false;

			// The following devices are explicitly ok.
			// Note: 'High' BlackBerry devices ONLY
			if (detectMobileQuick()) {

				// Exclude iPhone Tier and e-Ink Kindle devices.
				if (!detectTierIphone() && !detectKindle()) {

					// The following devices are explicitly ok.
					// Note: 'High' BlackBerry devices ONLY
					// Older Windows 'Mobile' isn't good enough for iPhone Tier.
					if (detectWebkit() || detectS60OssBrowser() || detectBlackBerryHigh() || detectWindowsMobile()
							|| userAgent.contains(engineTelecaQ)) {
						result = true;
					}
				}
			}
			return result;
		}

		/**
		 * 快速检测设备，该方法检测到所有其他类型的手机,但不包括iPhone和RichCSS层设备
		 *
		 * The quick way to detect for a tier of devices. This method detects
		 * for all other types of phones, but excludes the iPhone and RichCSS
		 * Tier devices.
		 *
		 * @return detection of a mobile device in the less capable tier 不常用设备
		 */
		public boolean detectTierOtherPhones() {
			if (this.initCompleted || this.isTierGenericMobile) {
				return this.isTierGenericMobile;
			}

			// Exclude devices in the other 2 categories
			return detectMobileLong() && !detectTierIphone() && !detectTierRichCss();

		}
	}

}
