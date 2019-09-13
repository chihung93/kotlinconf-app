import UIKit

class ScheduleTableSmallHeader : UITableViewHeaderFooterView {
    @IBOutlet weak var title: UILabel!

    func displayDay(title: String) {
        self.title.text = title
        self.title.textColor = UIColor.dayGray
    }

    func displayLunch(title: String) {
        self.title.text = title
        self.title.textColor = UIColor.redOrange
    }

}
