import UIKit

class WelcomeController : UIPageViewController, UIPageViewControllerDelegate {
    private(set) lazy var pages: [UIViewController] = {
        return [
            createPage(name: "WelcomePrivacyPolicyController"),
            createPage(name: "WelcomeLocationController"),
            createPage(name: "WelcomeNotificationsController")
        ]
    }()

    override func viewDidLoad() {
        dataSource = self
        delegate = self
        setViewControllers([pages.first!], direction: .forward, animated: true, completion: nil)

        view.backgroundColor = UIColor.defaultGray
        let dotsView = UIPageControl.appearance()
        dotsView.pageIndicatorTintColor = UIColor.dark20
        dotsView.currentPageIndicatorTintColor = UIColor.blackGray
    }
}

class WelcomePrivacyPolicyController : UIViewController {
    @IBAction func acceptTouch(_ sender: Any) {
        Conference.acceptPrivacyPolicy()
        navigationController?.popViewController(animated: true)
    }
    
    @IBAction func nextTouch(_ sender: Any) {
    }
}

class WelcomeLocationController : UIViewController {
    @IBAction func acceptTouch(_ sender: Any) {
    }

    @IBAction func nextTouch(_ sender: Any) {
    }

}

class WelcomeNotificationsController : UIViewController {
    @IBAction func acceptTouch(_ sender: Any) {
        Conference.requestNotificationPermissions()
    }

    @IBAction func closeTouch(_ sender: Any) {
        navigationController?.popViewController(animated: true)
    }
}


extension WelcomeController : UIPageViewControllerDataSource {

    func pageViewController(
        _ pageViewController: UIPageViewController,
        viewControllerBefore viewController: UIViewController
    ) -> UIViewController? {
        let index: Int = pages.firstIndex(of: viewController)!
        return getPage(index: index - 1)
    }

    func pageViewController(
        _ pageViewController: UIPageViewController,
        viewControllerAfter viewController: UIViewController
    ) -> UIViewController? {
        let index: Int = pages.firstIndex(of: viewController)!
        return getPage(index: index + 1)
    }

    private func getPage(index: Int) -> UIViewController? {
        if index < 0 || index >= pages.count {
            return nil
        }

        return pages[index]
    }

    func presentationCount(for pageViewController: UIPageViewController) -> Int {
        return pages.count
    }

    func presentationIndex(for pageViewController: UIPageViewController) -> Int {
        return pages.firstIndex(of: pageViewController) ?? 0
    }
}

func createPage(name: String) -> UIViewController {
    return UIStoryboard(name: "Main", bundle: nil).instantiateViewController(withIdentifier: name)
}
