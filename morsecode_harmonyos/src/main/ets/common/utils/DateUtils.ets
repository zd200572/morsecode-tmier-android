export class DateUtils {
  static formatTime(hour: number, minute: number): string {
    return `${String(hour).padStart(2, '0')} : ${String(minute).padStart(2, '0')}`;
  }

  static formatSecond(second: number): string {
    return String(second).padStart(2, '0');
  }

  static getCurrentTime(): [number, number, number, number, number, number] {
    const date = new Date();
    const year = date.getFullYear();
    const month = date.getMonth() + 1;
    const day = date.getDate();
    const hour = date.getHours();
    const minute = date.getMinutes();
    const second = date.getSeconds();
    return [year, month, day, hour, minute, second];
  }
}
