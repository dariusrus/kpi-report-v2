export class SharedUtil {
  static formatMonthAndYear(monthAndYear: string): string {
    const [month, year] = monthAndYear.split(', ');
    return `${month.slice(0, 3)}, ${year}`;
  }

  static percentageFormatting(data: any): string {
    if (data === 0) {return '0%'}
    if (data === undefined || data === null || data === 'N/A') {
      return '-';
    }
    return data + '%';
  }

  static valueFormatting(data: any): string {
    if (data.label === 'Opportunity-to-Lead') {
      return data.value.toFixed(2) + '%';
    }
    if (data.label === 'Lead Valuation') {
      return '$ ' + Math.round(data.value).toLocaleString();
    }
    if (!data.label) {
      return data + '%';
    }
    return Math.round(data.value).toLocaleString();
  }

  static currencyFormatting(data: any): string {
    return '$ ' + Math.round(data).toLocaleString();
  }

  static getInitials(name: string): string {
    if (!name || name === '') return 'N/A';
    const ignoreWords = ["and", "or"];
    let initials = name
      .split(' ')
      .filter(word => !ignoreWords.includes(word.toLowerCase()))
      .map(n => n[0])
      .join('');
    return initials.toUpperCase();
  }

  static getRandomColor(name: string): string {
    let hash = 0;
    for (let i = 0; i < name.length; i++) {
      hash = name.charCodeAt(i) + ((hash << 5) - hash);
    }
    let color = '#';
    for (let i = 0; i < 3; i++) {
      let value = (hash >> (i * 8)) & 0xFF;
      value = Math.floor((value * 0.4) + 153);
      color += ('00' + value.toString(16)).substr(-2);
    }
    return color;
  }

  static timeFormatting(data: number): string {
    if (data === 0) {return '0s'}
    const secondsInMinute = 60;
    const secondsInHour = secondsInMinute * 60;
    const secondsInDay = secondsInHour * 24;
    const secondsInWeek = secondsInDay * 7;

    const weeks = Math.floor(data / secondsInWeek);
    data %= secondsInWeek;

    const days = Math.floor(data / secondsInDay);
    data %= secondsInDay;

    const hours = Math.floor(data / secondsInHour);
    data %= secondsInHour;

    const minutes = Math.floor(data / secondsInMinute);
    const seconds = data % secondsInMinute;

    const parts = [];
    if (weeks > 0) parts.push(`${weeks}w`);
    if (days > 0) parts.push(`${days}d`);
    if (hours > 0) parts.push(`${hours}h`);
    if (minutes > 0) parts.push(`${minutes}m`);
    if (seconds > 0) parts.push(`${seconds}s`);

    return parts.join(' ');
  }
}
