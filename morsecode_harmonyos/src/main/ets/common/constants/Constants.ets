export class Constants {
  static readonly PREFS_NAME: string = 'morse_prefs';
  static readonly KEY_INTERVAL: string = 'schedule_interval';
  static readonly KEY_VIBRATION: string = 'enable_vibration';
  static readonly KEY_SOUND: string = 'enable_sound';
  static readonly KEY_FLASHLIGHT: string = 'enable_flashlight';
  static readonly KEY_INCLUDE_DATE: string = 'include_date';

  static readonly INTERVAL_15: number = 15;
  static readonly INTERVAL_30: number = 30;
  static readonly INTERVAL_60: number = 60;

  static readonly DEFAULT_VIBRATION: boolean = true;
  static readonly DEFAULT_SOUND: boolean = true;
  static readonly DEFAULT_FLASHLIGHT: boolean = false;
  static readonly DEFAULT_INCLUDE_DATE: boolean = false;
  static readonly DEFAULT_INTERVAL: number = 60;
}

export class TimeConstants {
  static readonly YEAR: number = 0;
  static readonly MONTH: number = 1;
  static readonly DAY: number = 2;
  static readonly HOUR: number = 3;
  static readonly MINUTE: number = 4;
  static readonly SECOND: number = 5;
}
