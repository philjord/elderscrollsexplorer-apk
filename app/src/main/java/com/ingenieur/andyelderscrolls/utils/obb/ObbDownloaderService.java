package com.ingenieur.andyelderscrolls.utils.obb;

import com.google.android.vending.expansion.downloader.impl.DownloaderService;

/**
 * Created by phil on 12/18/2016.
 */

public class ObbDownloaderService extends DownloaderService
{
		// You must use the public key belonging to your publisher account
		public static final String BASE64_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAqoEA2+dtSDgAZZhwOIhf67H2xR8rvrLENhrI5zNl8W7+GfGsRxfMmGiwisuOASY8fBh+t5IZumP7WGJ418oML6rUBpUCNihDuZcS/OrNQky7RyFkoY16n1G3v+jm4UwLoEsNQJnEpBWvPy0hptT6qRpRhNI7SVYilzPBc7FQPG2NWKh6kNoqSVoPI3K5hRzIYtqRtkHhFtMvpZhxcQuzKptLDu0ceCyEQLeWJmtiO1yCd57zkG0R+sIWd+69uuORIJGmg8vJWljyBTdhrKB8+sg3SZh4S/6lj0GZpy+M7cpzoJC4aBRVN/YMDxax1c56l7T8AY63pcCou8Ai20ER8QIDAQAB";
		// You should also modify this salt
		public static final byte[] SALT = new byte[] { -49, 33, -87, -1, 52, -6,
				-100, -5, 43, 62, -88, -4, 96, 5, -106, 77, -33, 70, -1, 84
		};

		@Override
		public String getPublicKey() {
			return BASE64_PUBLIC_KEY;
		}

		@Override
		public byte[] getSALT() {
			return SALT;
		}

		@Override
		public String getAlarmReceiverClassName() {
			return ObbAlarmServiceReceiver.class.getName();
		}
	}
