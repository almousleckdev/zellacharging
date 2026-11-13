# Phone verification by WhatsApp or SMS (Twilio)

The customer chooses how to receive the 6-digit code, `WHATSAPP` (recommended) or `SMS`.

- `POST /v1/me/phone` `{phone, delivery}`: sends the code and keeps the new number pending.
- `POST /v1/me/phone/code` `{delivery}`: sends a code for the number already on the account (the Verify button) or the pending one.
- `POST /v1/me/phone/verify` `{code}`: checks the code, then attaches the number (if it was pending) and marks it verified. Codes expire and are
limited by the same rules as the email codes (`zella.verification`).

## Where it is used

- **Register** (`POST /v1/auth/register`): an email or a phone number (E.164 from the app's country picker, Gabon by default).
  The response says which contact to verify (`channel`, `target`); a phone registration takes `delivery`.
- **Verify / resend** (`/v1/auth/verify`, `/v1/auth/verify/resend`): `channel` is `EMAIL` or `PHONE`; resend takes `delivery`.
- **Forgot / reset password** (`/v1/auth/password/forgot|reset`): `identifier` is an email or a phone number (`email` still works).
- **Login** (`/v1/auth/login`): `identifier` is an email or a phone number.
- **Profile**: the customer can add, verify or change the email and the phone (`/v1/me/email`, `/v1/me/phone`); the new
  contact only replaces the current one once its code is confirmed.

## Variables (backend `.env` and Railway)

Shared by WhatsApp and SMS: `ZELLA_TWILIO_COMPTE`, `ZELLA_TWILIO_CLE`, `ZELLA_TWILIO_SECRET`.

| Variable | Twilio value |
| --- | --- |
| `ZELLA_TWILIO_COMPTE` | Account SID (`AC...`) |
| `ZELLA_TWILIO_CLE` | API key SID (`SK...`) |
| `ZELLA_TWILIO_SECRET` | API key secret |
| `ZELLA_TWILIO_SERVICE` | Messaging Service SID (`MG...`) holding the WhatsApp sender |
| `ZELLA_TWILIO_MODELE` | Content template SID (`HX...`), an approved WhatsApp authentication template whose variable `{{1}}` is the code |
| `ZELLA_TWILIO_MODELE_EN` | optional: the English version of the template, used only when the app language is English (otherwise `ZELLA_TWILIO_MODELE`, which must be the **French** template, is used) |
| `ZELLA_TWILIO_SMS_SERVICE` | Messaging Service SID (`MG...`) holding an SMS-capable sender, **or** |
| `ZELLA_TWILIO_SMS_NUMERO` | a Twilio phone number in E.164 (`+1...`) used as `From` when no SMS service is set |

The message is sent with `POST /2010-04-01/Accounts/{COMPTE}/Messages.json` (basic auth `CLE:SECRET`, fields `To=whatsapp:+E164`,
`MessagingServiceSid`, `ContentSid`, `ContentVariables={"1":"<code>"}`).

## Behaviour

- WhatsApp needs `COMPTE`, `CLE`, `SECRET`, `SERVICE`, `MODELE`; SMS needs `COMPTE`, `CLE`, `SECRET` and one of `SMS_SERVICE` or `SMS_NUMERO`. When the variables of a delivery are set, the code goes out through Twilio. The SMS text is `sms.verification.body` in `errors.properties` (French and English). A Twilio refusal or timeout answers `503 CODE_NOT_SENT` and the
  number stays unattached; the customer asks again from the app.
- A delivery whose variables are missing (local development): nothing is sent and the code is written to the backend log.
- Twilio must be allowed to send SMS to the customer's country (geo permissions), and Gabon senders may need a registered alphanumeric sender or a local number.
