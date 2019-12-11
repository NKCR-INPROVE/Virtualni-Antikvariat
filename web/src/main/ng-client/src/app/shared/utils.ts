export class Utils {

  /**
   * Method copies source properties to target only if they exist in target
   * @param source the source object
   * @param target the target
   */

  public static sanitize(source, target) {
    for (const p in source) {
      if (source[p] && p in target) {
        target[p] = source[p];
      }
    }
  }

  public static generateAvatar(name: string): string {
    let ret = '';
    const words = name.split(' ');

    if (words.length < 3) {
      words.forEach(s => {
        ret += s[0].toUpperCase() + s[1].toUpperCase();
      });
    } else {
      words.forEach(s => ret += s[0].toUpperCase());
    }
    return ret;
  }
}
